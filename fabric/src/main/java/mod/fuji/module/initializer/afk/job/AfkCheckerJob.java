package mod.fuji.module.initializer.afk.job;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.job.JobManager;
import mod.fuji.module.initializer.afk.AfkInitializer;
import mod.fuji.module.initializer.afk.service.AfkService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1751826177457L, value = """
    This `job` is used to compare and mark a player as in afk state.
    """)
public class AfkCheckerJob extends CronJob {

    public AfkCheckerJob() {
        super(() -> AfkInitializer.config.model().getAfkChecker().getCron());
    }

    @EventConsumer
    private static void scheduleAfkCheckerJob(@Unused ServerStartedEvent event) {
        JobManager.addJob(new AfkCheckerJob());
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        PlayerHelper.Lookup.getOnlinePlayers()
            .stream()
            .filter(it -> !it.isRemoved())
            .forEach(it -> {
                /* Update previous input counter. */
                long prevInputCounter = AfkService.getPreviousInputCounter(it);
                long curInputCounter = it.getLastActionTime();
                AfkService.setPreviousInputCounter(it, curInputCounter);

                /* If the two consecutive values are identical, enter the afk state. */
                if (prevInputCounter == curInputCounter && !AfkService.isInAfkState(it)) {
                    AfkService.changeAfkState(it, true);
                }
            });

    }
}
