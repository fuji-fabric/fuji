package io.github.sakurawald.core.job.abst;

import lombok.NoArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.TriggerBuilder;

import java.util.function.Supplier;

/*
 * Quartz will create a new instance of CronJob each time it calls execute(), so don't store persistent data inside the instance.
* */
@NoArgsConstructor
public abstract class CronJob extends BaseJob {

    Supplier<String> cronSupplier;

    public CronJob(String jobGroup, String jobName, JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        super(jobGroup, jobName, jobDataMap);
        this.cronSupplier = cronSupplier;
    }

    public CronJob(JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        super(null, null, jobDataMap);
        this.cronSupplier = cronSupplier;
    }

    public CronJob(Supplier<String> cronSupplier) {
        this(null, cronSupplier);
    }

    @Override
    public CronTrigger makeTrigger() {
        return TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).withSchedule(CronScheduleBuilder.cronSchedule(this.cronSupplier.get())).build();
    }
}
