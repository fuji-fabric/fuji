package mod.fuji.module.initializer.command_state.job;


import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.job.JobManager;
import mod.fuji.module.initializer.command_state.service.CommandStateService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1756693438608L, value = """
    This `job` is used to `check` and `update` the value of all the defined `states` for all online players.

    When fired, its effect is equivalent to running the `/command-state update-all` command.
    """)
public class CommandStateAutoUpdaterJob extends CronJob {

    public CommandStateAutoUpdaterJob() {
        super(() -> JobManager.CRON_EVERY_SECOND);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ServerHelper.executeSync(CommandStateService::updateAllCommandStates);
    }

    @EventConsumer
    private static void addSelf(@Unused ServerStartedEvent event) {
        JobManager.addJob(new CommandStateAutoUpdaterJob());
    }
}
