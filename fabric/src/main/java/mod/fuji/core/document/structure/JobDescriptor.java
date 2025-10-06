package mod.fuji.core.document.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import mod.fuji.core.document.interfaces.SourceModuleGetter;
import mod.fuji.core.manager.impl.module.ModulePathResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import mod.fuji.core.job.JobManager;
import org.jetbrains.annotations.NotNull;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.List;
import org.quartz.impl.matchers.GroupMatcher;

@Data
@AllArgsConstructor
public class JobDescriptor implements SourceModuleGetter {
    public final JobDetail jobDetail;
    List<? extends Trigger> triggersOfJob;

    public static @NotNull List<JobDescriptor> getJobDescriptors() throws SchedulerException {
        List<JobDescriptor> entities = new ArrayList<>();

        /* Get all jobs. */
        Scheduler scheduler = JobManager.getScheduler();

        // NOTE: Match all jobs, including `CronJob` and `FixedIntervalJob`.
        GroupMatcher<JobKey> jobKeyGroupMatcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(jobKeyGroupMatcher);
        for (JobKey jobKey : jobKeys) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
            entities.add(new JobDescriptor(jobDetail, triggersOfJob));
        }

        entities.sort(Comparator.comparing(it -> it.getJobDetail().getKey().getGroup()));
        return entities;
    }

    @Override
    public String getSourceModule() {
        JobDetail jobDetail = this.jobDetail;

        /* Try to find the source module from the data map. */
        Object specifiedSourceModule = jobDetail.getJobDataMap().get(SPECIFIED_SOURCE_MODULE_KEY);
        if (specifiedSourceModule != null) {
            return specifiedSourceModule.toString();
        }

        /* No source module is specified, try to compute it from the job class. */
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        return ModulePathResolver.computeModulePathString(jobClass.getName());
    }

}
