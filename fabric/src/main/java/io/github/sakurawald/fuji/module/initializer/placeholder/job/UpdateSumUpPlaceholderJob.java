package io.github.sakurawald.fuji.module.initializer.placeholder.job;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.module.initializer.placeholder.structure.SumUpPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

@Document(id = 1751826509334L, value = """
    This `job` is used to `update` the `sum up placeholders` of the `server`.
    """)
public class UpdateSumUpPlaceholderJob extends CronJob {

    public UpdateSumUpPlaceholderJob() {
        super(() -> ScheduleManager.CRON_EVERY_MINUTE);
    }

    @EventConsumer
    private static void scheduleSumUpPlaceholderUpdaterJob(@Unused ServerStartedEvent event) {
        SumUpPlaceholder.ofServer();
        UpdateSumUpPlaceholderJob updateSumUpPlaceholderJob = new UpdateSumUpPlaceholderJob();
        Managers.getScheduleManager().scheduleJob(updateSumUpPlaceholderJob);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        // save all online-player's stats into /stats/ folder
        PlayerHelper.Lookup.getOnlinePlayers().forEach((p) -> p.getStatHandler().save());

        // update
        SumUpPlaceholder.ofServer();
    }
}
