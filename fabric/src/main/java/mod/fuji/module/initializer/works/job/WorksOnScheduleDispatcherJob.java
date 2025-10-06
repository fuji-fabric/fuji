package mod.fuji.module.initializer.works.job;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.job.abst.CronJob;
import mod.fuji.core.job.interfaces.Schedulable;
import mod.fuji.core.manager.impl.scheduler.ScheduleManager;
import mod.fuji.module.initializer.works.structure.WorksBinding;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Document(id = 1751825531415L, value = """
    This `job` is used to dispatch the `onSchedule` event for each `work`.

    For example:
    1. To end the sample of a `production work`.
    """)

@NoArgsConstructor
public class WorksOnScheduleDispatcherJob extends CronJob {

    private WorksOnScheduleDispatcherJob(Supplier<String> cronSupplier) {
        super(cronSupplier);
    }

    public static WorksOnScheduleDispatcherJob makeInstance() {
        return new WorksOnScheduleDispatcherJob(() -> ScheduleManager.CRON_EVERY_FIVE_SECONDS);
    }

    @EventConsumer
    private static void scheduleWorksOnScheduleDispatcherJob(@Unused ServerStartedEvent event) {
        WorksOnScheduleDispatcherJob job = makeInstance();
        ScheduleManager.addJob(job);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        Set<Work> uniqueWorks = new HashSet<>();
        WorksBinding.BLOCK_POS_2_WORKS.values().forEach(uniqueWorks::addAll);
        WorksBinding.ENTITY_2_WORKS.values().forEach(uniqueWorks::addAll);
        uniqueWorks.forEach(it -> {
            if (it instanceof Schedulable schedulable) {
                schedulable.onSchedule();
            }
        });
    }
}
