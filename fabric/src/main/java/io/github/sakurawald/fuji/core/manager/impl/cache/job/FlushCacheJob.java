package io.github.sakurawald.fuji.core.manager.impl.cache.job;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1756546983124L, value = """
    This `job` is used to `flush` the `cache data` from `memory` into `storage`.

    The `cache files` are located in `config/fuji/cache/` directory.
    """)
public class FlushCacheJob extends CronJob {

    public FlushCacheJob() {
        super(() -> ScheduleManager.CRON_EVERY_MINUTE);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Managers.getCacheManager().flushGenericCacheModels();
    }
}
