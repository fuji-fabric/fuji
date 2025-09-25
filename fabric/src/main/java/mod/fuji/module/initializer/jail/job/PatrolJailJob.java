package mod.fuji.module.initializer.jail.job;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.abst.FixedIntervalJob;
import mod.fuji.core.manager.Managers;
import mod.fuji.module.initializer.jail.service.JailService;
import mod.fuji.module.initializer.jail.structure.JailDescriptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1753752697817L, value = """
    This `job` is used to `execute the patrol commands` for a `jail` periodically.
    """)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PatrolJailJob extends FixedIntervalJob {

    public PatrolJailJob(@NotNull JobDataMap jobDataMap, @NotNull JailDescriptor jailDescriptor) {
        super(null, jailDescriptor.getId(), jobDataMap, jailDescriptor.getPatrol().getPatrolIntervalMillSeconds(), REPEAT_INDEFINITELY);
        super.rescheduleAble = false;
    }

    public static void scheduleJob(@NotNull JailDescriptor jailDescriptor) {
        /* Make the job. */
        PatrolJailJob job = new PatrolJailJob(new JobDataMap() {
            {
                this.put(JailDescriptor.class.getName(), jailDescriptor);
            }
        }, jailDescriptor);

        /* Schedule the job. */
        Managers.getScheduleManager().addJob(job);
    }

    public static void reloadPatrolJobs() {
        /* Un-schedule jobs. */
        LogUtil.debug("Un-schedule patrol jobs.");
        Managers.getScheduleManager().deleteJobs(PatrolJailJob.class);

        JailService
            .getJailDescriptors()
            .forEach(jailDescriptor -> {
                String jailId = jailDescriptor.getId();
                int patrolIntervalMillSeconds = jailDescriptor.getPatrol().getPatrolIntervalMillSeconds();
                LogUtil.debug("Schedule patrol job: jailId = {}, intervalMillSeconds = {}", jailId, patrolIntervalMillSeconds);
                scheduleJob(jailDescriptor);
            });
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JailDescriptor jailDescriptor = (JailDescriptor) context.getJobDetail().getJobDataMap().get(JailDescriptor.class.getName());
        JailService.executePatrolCommands(jailDescriptor);
    }
}
