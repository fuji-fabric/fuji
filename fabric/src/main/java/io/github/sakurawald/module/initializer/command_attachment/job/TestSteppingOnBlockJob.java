package io.github.sakurawald.module.initializer.command_attachment.job;

import io.github.sakurawald.core.job.abst.CronJob;
import io.github.sakurawald.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.module.initializer.command_attachment.CommandAttachmentInitializer;
import org.quartz.JobExecutionContext;

public class TestSteppingOnBlockJob extends CronJob {

    public TestSteppingOnBlockJob() {
        super(() -> ScheduleManager.CRON_EVERY_SECOND);
    }

    @Override
    public void execute(JobExecutionContext context) {
        CommandAttachmentInitializer.testSteppingBlockForPlayers();
    }
}
