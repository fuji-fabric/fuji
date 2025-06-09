package io.github.sakurawald.module.initializer.placeholder.job;

import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.job.abst.CronJob;
import io.github.sakurawald.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.module.initializer.placeholder.structure.SumUpPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

public class UpdateSumUpPlaceholderJob extends CronJob {

    public UpdateSumUpPlaceholderJob() {
        super(() -> ScheduleManager.CRON_EVERY_MINUTE);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        // save all online-player's stats into /stats/ folder
        ServerHelper.getPlayers().forEach((p) -> p.getStatHandler().save());

        // update
        SumUpPlaceholder.ofServer();
    }
}
