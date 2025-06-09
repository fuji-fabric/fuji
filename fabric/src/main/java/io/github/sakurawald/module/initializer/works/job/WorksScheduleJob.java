package io.github.sakurawald.module.initializer.works.job;

import io.github.sakurawald.core.job.abst.CronJob;
import io.github.sakurawald.module.initializer.works.structure.WorksBinding;
import io.github.sakurawald.module.initializer.works.structure.work.abst.Work;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@NoArgsConstructor
public class WorksScheduleJob extends CronJob {

    public WorksScheduleJob(JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        super(jobDataMap, cronSupplier);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        Set<Work> activeWorks = new HashSet<>();
        WorksBinding.getBlockpos2works().values().forEach(activeWorks::addAll);
        WorksBinding.getEntity2works().values().forEach(activeWorks::addAll);
        activeWorks.forEach(Work::onSchedule);
    }
}
