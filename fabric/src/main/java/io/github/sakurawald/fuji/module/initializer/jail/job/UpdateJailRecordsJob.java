package io.github.sakurawald.fuji.module.initializer.jail.job;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.job.abst.FixedIntervalJob;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
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
    private static void scheduleUpdateJailRecordsJob(@Unused ServerStartedEvent event) {
        UpdateJailRecordsJob updateJailRecordsJob = new UpdateJailRecordsJob();
        Managers.getScheduleManager().scheduleJob(updateJailRecordsJob);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JailService.updateJailRecords(getUpdateJailRecordsJobIntervalMillSeconds());
    }

    public static int getUpdateJailRecordsJobIntervalMillSeconds() {
        return 1000;
    }
}
