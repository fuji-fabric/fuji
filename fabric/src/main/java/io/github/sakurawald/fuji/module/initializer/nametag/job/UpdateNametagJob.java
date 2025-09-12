package io.github.sakurawald.fuji.module.initializer.nametag.job;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.nametag.NametagInitializer;
import io.github.sakurawald.fuji.module.initializer.nametag.service.NametagService;
import org.quartz.JobExecutionContext;

@Document(id = 1751825006787L, value = """
    This `job` is used to `update` the nametag for each player.
    """)
public class UpdateNametagJob extends CronJob {

    public UpdateNametagJob() {
        super(() -> NametagInitializer.config.model().update_cron);
    }

    @Override
    public void execute(JobExecutionContext context) {
        NametagService.processNametagEntities();
    }

    @EventConsumer
    private static void scheduleUpdateNametagJob(@Unused ServerStartedEvent event) {
        UpdateNametagJob updateNametagJob = new UpdateNametagJob();
        Managers.getScheduleManager().scheduleJob(updateNametagJob);
    }

}
