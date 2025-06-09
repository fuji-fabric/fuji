package io.github.sakurawald.module.initializer.command_scheduler.job;

import io.github.sakurawald.core.job.abst.CronJob;
import io.github.sakurawald.module.initializer.command_scheduler.structure.Job;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.function.Supplier;

@NoArgsConstructor
public class CommandScheduleJob extends CronJob {

    public CommandScheduleJob(JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        super(jobDataMap, cronSupplier);

        // we will handle the un-register ourselves.
        super.reschedulable = false;
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        Job job = (Job) context.getJobDetail().getJobDataMap().get("job");
        job.trigger();
    }
}
