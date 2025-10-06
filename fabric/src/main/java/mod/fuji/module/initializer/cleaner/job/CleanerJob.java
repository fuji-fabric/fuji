package mod.fuji.module.initializer.cleaner.job;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.job.JobManager;
import mod.fuji.module.initializer.cleaner.CleanerInitializer;
import mod.fuji.module.initializer.cleaner.service.CleanerService;
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
        JobManager.addJob(cleanerJob);
    }

    @Override
    public void execute(JobExecutionContext context) {
        ServerHelper.executeSync(CleanerService::cleanEntities);
    }
}
