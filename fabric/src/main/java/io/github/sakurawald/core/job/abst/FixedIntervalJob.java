package io.github.sakurawald.core.job.abst;

import lombok.NoArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@NoArgsConstructor
public abstract class FixedIntervalJob extends BaseJob {

    int intervalMs;
    int repeatCount;

    public FixedIntervalJob(String jobGroup, String jobName, JobDataMap jobDataMap, int intervalMs, int repeatCount) {
        super(jobGroup, jobName, jobDataMap);
        this.intervalMs = intervalMs;
        this.repeatCount = repeatCount;

        // modify it to false
        super.reschedulable = false;
    }

    @Override
    public Trigger makeTrigger() {
        return TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(intervalMs).withRepeatCount(repeatCount - 1)).build();
    }

}
