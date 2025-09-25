package mod.fuji.module.initializer.jail.job;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.job.abst.FixedIntervalJob;
import mod.fuji.core.manager.Managers;
import mod.fuji.module.initializer.jail.service.JailService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Document(id = 1753694703368L, value = """
    This job is used to update all active `jail records` across all `jails`.
    """)
public class UpdateJailRecordsJob extends FixedIntervalJob {

    public UpdateJailRecordsJob() {
        super(null, null, null, getUpdateJailRecordsJobIntervalMillSeconds() , REPEAT_INDEFINITELY);
    }

    @EventConsumer
    private static void reloadUpdateJailRecordsJob(@Unused ServerStartedEvent event) {
        UpdateJailRecordsJob updateJailRecordsJob = new UpdateJailRecordsJob();
        Managers.getScheduleManager().addJob(updateJailRecordsJob);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JailService.updateJailRecords(getUpdateJailRecordsJobIntervalMillSeconds());
    }

    public static int getUpdateJailRecordsJobIntervalMillSeconds() {
        return 1000;
    }
}
