package io.github.sakurawald.fuji.module.initializer.tab.job;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.module.initializer.tab.TabListInitializer;
import org.quartz.JobExecutionContext;

@Document("""
    This `job` is used to `update` the `header` and `footer` in `tab` list.
    """)
public class RenderHeaderAndFooterJob extends CronJob {

    public RenderHeaderAndFooterJob() {
        super(() -> TabListInitializer.config.model().update_cron);
    }

    @Override
    public void execute(JobExecutionContext context) {
        TabListInitializer.render();
    }
}
