package io.github.sakurawald.fuji.module.initializer.cleaner.job;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.cleaner.CleanerInitializer;
import io.github.sakurawald.fuji.module.initializer.cleaner.service.CleanerService;
import org.quartz.JobExecutionContext;

@Document(id = 1751826895787L, value = """
    This `job` is used to execute `/cleaner clean` command automatically.
    """)
public class CleanerJob extends CronJob {

    public CleanerJob() {
        super(() -> CleanerInitializer.config.model().getCron());
    }

    @EventConsumer
    private static void scheduleCleanerJob(@Unused ServerStartedEvent event) {
        CleanerJob cleanerJob = new CleanerJob();
        Managers.getScheduleManager().scheduleJob(cleanerJob);
    }

    @Override
    public void execute(JobExecutionContext context) {
        ServerHelper.executeSync(CleanerService::cleanEntities);
    }
}
