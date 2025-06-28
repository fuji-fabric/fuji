package io.github.sakurawald.fuji.module.initializer.command_scheduler.job;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.structure.Job;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.function.Supplier;

@Document("""
    This `job` is defined by `command_schedule` module.
    And used to `execute specified commands`.
    """)

@NoArgsConstructor
public class CommandScheduleJob extends CronJob {

    public CommandScheduleJob(String definedJobName, JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        super("command_scheduler", definedJobName, jobDataMap, cronSupplier);

        // NOTE: We will handle the un-register ourselves.
        super.canReschedule = false;
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        Job job = (Job) context.getJobDetail().getJobDataMap().get("job");
        job.tryTrigger();
    }
}
