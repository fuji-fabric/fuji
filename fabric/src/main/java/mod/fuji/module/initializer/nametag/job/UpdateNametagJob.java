package mod.fuji.module.initializer.nametag.job;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.manager.Managers;
import mod.fuji.module.initializer.nametag.NametagInitializer;
import mod.fuji.module.initializer.nametag.service.NametagService;
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
        NametagService.refreshNametagEntities();
    }

    @EventConsumer
    private static void scheduleUpdateNametagJob(@Unused ServerStartedEvent event) {
        UpdateNametagJob updateNametagJob = new UpdateNametagJob();
        Managers.getScheduleManager().scheduleJob(updateNametagJob);
    }

}
