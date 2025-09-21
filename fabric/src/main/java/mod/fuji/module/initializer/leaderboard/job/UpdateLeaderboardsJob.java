package mod.fuji.module.initializer.leaderboard.job;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.manager.impl.scheduler.ScheduleManager;
import mod.fuji.module.initializer.leaderboard.service.LeaderBoardService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1753491464352L, value = """
    This `job` is used to execute `/leaderboard update-all` command automatically.
    """)
public class UpdateLeaderboardsJob extends CronJob {

    public UpdateLeaderboardsJob() {
        super(() -> ScheduleManager.CRON_EVERY_MINUTE);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LeaderBoardService.updateLeaderBoards();
    }
}
