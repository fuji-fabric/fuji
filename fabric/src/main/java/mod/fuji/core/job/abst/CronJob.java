package mod.fuji.core.job.abst;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.TriggerBuilder;

import java.util.function.Supplier;

@NoArgsConstructor
public abstract class CronJob extends BaseJob {

    Supplier<String> cronSupplier;

    public CronJob(@Nullable String jobGroup, @Nullable String jobName, @Nullable JobDataMap jobDataMap, @NotNull Supplier<String> cronSupplier, boolean staticJob) {
        super(jobGroup, jobName, jobDataMap, staticJob);
        this.cronSupplier = cronSupplier;
    }

    public CronJob(@Nullable String jobGroup, @Nullable String jobName, @Nullable JobDataMap jobDataMap, @NotNull Supplier<String> cronSupplier) {
        this(jobGroup, jobName, jobDataMap, cronSupplier, true);
    }

    public CronJob(@Nullable JobDataMap jobDataMap, @NotNull Supplier<String> cronSupplier) {
        this(null, null, jobDataMap, cronSupplier);
    }

    public CronJob(@NotNull Supplier<String> cronSupplier) {
        this(null, cronSupplier);
    }

    @Override
    public @NotNull CronTrigger makeTrigger() {
        return TriggerBuilder
            .newTrigger()
            .withIdentity(this.getTriggerKey())
            .withSchedule(CronScheduleBuilder.cronSchedule(this.cronSupplier.get()))
            .build();
    }
}
