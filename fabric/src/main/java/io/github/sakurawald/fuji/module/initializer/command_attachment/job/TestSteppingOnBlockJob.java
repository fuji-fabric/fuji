package io.github.sakurawald.fuji.module.initializer.command_attachment.job;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
import org.quartz.JobExecutionContext;

@Document(id = 1751826425009L, value = """
    This `job` is used to test if the player is stepping on a `block` with `attached commands`.
    """)
public class TestSteppingOnBlockJob extends CronJob {

    public TestSteppingOnBlockJob() {
        super(() -> ScheduleManager.CRON_EVERY_SECOND);
    }

    @Override
    public void execute(JobExecutionContext context) {
        CommandAttachmentInitializer.testSteppingBlockForPlayers();
    }
}
