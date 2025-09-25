package mod.fuji.module.initializer.command_scheduler.job;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.module.initializer.command_scheduler.structure.CommandSchedulerJobDescriptor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.function.Supplier;

@Document(id = 1751826752357L, value = """
    This `job` is defined by `command_schedule` module.
    And used to `execute specified commands`.
    """)

@NoArgsConstructor
public class CommandScheduleJob extends CronJob {

    public CommandScheduleJob(String definedJobName, JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        // NOTE: Use the class name as the group name, to be consistent with the deleteJobs() method.
        super(null, definedJobName, jobDataMap, cronSupplier, false);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        CommandSchedulerJobDescriptor job = (CommandSchedulerJobDescriptor) context.getJobDetail().getJobDataMap().get("job");
        job.tryTrigger();
    }
}
