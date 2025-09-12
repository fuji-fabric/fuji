package io.github.sakurawald.fuji.module.initializer.works.job;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.core.job.interfaces.Schedulable;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.module.initializer.works.structure.WorksBinding;
import io.github.sakurawald.fuji.module.initializer.works.structure.work.abst.Work;
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
        super(null, cronSupplier);
    }

    public static WorksOnScheduleDispatcherJob makeInstance() {
        return new WorksOnScheduleDispatcherJob(() -> ScheduleManager.CRON_EVERY_FIVE_SECONDS);
    }

    @EventConsumer
    private static void scheduleWorksOnScheduleDispatcherJob(@Unused ServerStartedEvent event) {
        WorksOnScheduleDispatcherJob job = makeInstance();
        Managers.getScheduleManager().scheduleJob(job);
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
