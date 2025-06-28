package io.github.sakurawald.fuji.module.initializer.works.job;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.abst.CronJob;
import io.github.sakurawald.fuji.module.initializer.works.structure.WorksBinding;
import io.github.sakurawald.fuji.module.initializer.works.structure.work.abst.Work;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Document("""
    This `job` is used to `end` the `sample` for each `production work`.
    """)

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
