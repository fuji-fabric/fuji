package io.github.sakurawald.fuji.module.initializer.cleaner.job;

import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.module.initializer.cleaner.CleanerInitializer;
import org.quartz.JobExecutionContext;

public class CleanerJob extends CronJob {

    public CleanerJob() {
        super(() -> CleanerInitializer.config.model().cron);
    }

    @Override
    public void execute(JobExecutionContext context) {
        CleanerInitializer.clean();
    }
}
