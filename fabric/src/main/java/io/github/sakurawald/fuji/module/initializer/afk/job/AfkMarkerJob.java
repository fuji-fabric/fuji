package io.github.sakurawald.fuji.module.initializer.afk.job;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.service.AfkService;
import io.github.sakurawald.fuji.module.initializer.afk.structure.PlayerAfkState;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1751826177457L, value = """
    This `job` is used to check the last action time for each player.
    """)
public class AfkMarkerJob extends CronJob {

    public AfkMarkerJob() {
        super(() -> AfkInitializer.config.model().afk_checker.cron);
    }

    @EventConsumer
    private static void scheduleAfkMarkerJob(@Unused ServerStartedEvent event) {
        AfkMarkerJob afkMarkerJob = new AfkMarkerJob();
        Managers.getScheduleManager().scheduleJob(afkMarkerJob);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        PlayerHelper.Lookup.getOnlinePlayers()
            .stream()
            .filter(it -> !it.isRemoved())
            .forEach(it -> {

                /* Update previous input counter. */
                PlayerAfkState playerAfkState = AfkService.getPlayerAfkState(it);
                long prevInputCounter = playerAfkState
                    .getPreviousInputCounter();
                long curInputCounter = it.getLastActionTime();
                playerAfkState.setPreviousInputCounter(curInputCounter);

                /* process */
                if (prevInputCounter == curInputCounter && !playerAfkState.isAfk()) {
                    AfkService.changeAfk(it, true);
                }

            });

    }
}
