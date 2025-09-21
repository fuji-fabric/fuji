package mod.fuji.core.manager.impl.cache.job;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.manager.Managers;
import mod.fuji.core.manager.impl.scheduler.ScheduleManager;
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

    @EventConsumer
    private static void scheduleFlushCacheJob(@Unused ServerStartedEvent event) {
        Managers.getScheduleManager().scheduleJob(new FlushCacheJob());
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Managers.getCacheManager().flushGenericCacheModels();
    }
}
