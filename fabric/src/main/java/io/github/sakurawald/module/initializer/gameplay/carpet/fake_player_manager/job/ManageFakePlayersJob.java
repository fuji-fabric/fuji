package io.github.sakurawald.module.initializer.gameplay.carpet.fake_player_manager.job;

import io.github.sakurawald.core.job.abst.CronJob;
import io.github.sakurawald.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.module.initializer.gameplay.carpet.fake_player_manager.FakePlayerManagerInitializer;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

public class ManageFakePlayersJob extends CronJob {

    public ManageFakePlayersJob() {
        super(() -> ScheduleManager.CRON_EVERY_MINUTE);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        FakePlayerManagerInitializer.checkCapsLimit();
    }

}
