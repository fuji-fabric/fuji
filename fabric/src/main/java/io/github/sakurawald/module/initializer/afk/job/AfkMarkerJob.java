package io.github.sakurawald.module.initializer.afk.job;

import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.job.abst.CronJob;
import io.github.sakurawald.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.module.initializer.afk.accessor.AfkStateAccessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AfkMarkerJob extends CronJob {

    public AfkMarkerJob() {
        super(() -> AfkInitializer.config.model().afk_checker.cron);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ServerHelper.getPlayers()
            .stream()
            .filter(it -> !it.isRemoved())
            .forEach(it -> {

                /* update input counter */
                String key = it.getGameProfile().getName();

                long prevInputCounter = AfkInitializer.player2prevInputCounter.computeIfAbsent(key, k -> -1L);
                long curInputCounter = ((AfkStateAccessor) it).fuji$getInputCounter();

                AfkInitializer.player2prevInputCounter.put(key, curInputCounter);

                /* process */
                AfkStateAccessor afkPlayer = (AfkStateAccessor) it;
                if (prevInputCounter == curInputCounter
                    && !afkPlayer.fuji$isAfk()) {
                    afkPlayer.fuji$changeAfk(true);
                }

            });

    }
}
