package io.github.sakurawald.fuji.module.initializer.afk.job;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.accessor.AfkStateAccessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1751826177457L, value = """
    This `job` is used to check the last action time for each player.
    """)
public class AfkMarkerJob extends CronJob {

    public AfkMarkerJob() {
        super(() -> AfkInitializer.config.model().afk_checker.cron);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        PlayerHelper.Lookup.getOnlinePlayers()
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
