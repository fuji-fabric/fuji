package io.github.sakurawald.fuji.module.initializer.command_state.job;


import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.module.initializer.command_state.service.CommandStateService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1756693438608L, value = """
    This `job` is used to `check` and `update` the value of all the defined `states` for all online players.

    When fired, its effect is equivalent to running the `/command-state update-all` command.
    """)
public class CommandStateAutoUpdaterJob extends CronJob {

    public CommandStateAutoUpdaterJob() {
        super(() -> ScheduleManager.CRON_EVERY_SECOND);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ServerHelper.executeSync(CommandStateService::updateAllCommandStates);
    }
}
