package mod.fuji.core.job.abst;

import lombok.NoArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.TriggerBuilder;

import java.util.function.Supplier;

@NoArgsConstructor
public abstract class CronJob extends BaseJob {

    Supplier<String> cronSupplier;

    public CronJob(String jobGroup, String jobName, JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        super(jobGroup, jobName, jobDataMap, true);
        this.cronSupplier = cronSupplier;
    }

    public CronJob(JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        this(null, null, jobDataMap, cronSupplier);
    }

    public CronJob(Supplier<String> cronSupplier) {
        this(null, cronSupplier);
    }

    @Override
    public CronTrigger makeTrigger() {
        return TriggerBuilder
            .newTrigger()
            .withIdentity(jobName, jobGroup)
            .withSchedule(CronScheduleBuilder.cronSchedule(this.cronSupplier.get()))
            .build();
    }
}
