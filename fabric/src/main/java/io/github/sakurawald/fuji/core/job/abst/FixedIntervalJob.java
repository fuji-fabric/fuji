package io.github.sakurawald.fuji.core.job.abst;

import lombok.NoArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@NoArgsConstructor
public abstract class FixedIntervalJob extends BaseJob {

    int intervalInMillSeconds;
    int repeatCount;

    public FixedIntervalJob(String jobGroup, String jobName, JobDataMap jobDataMap, int intervalInMillSeconds, int repeatCount) {
        super(jobGroup, jobName, jobDataMap, false);
        this.intervalInMillSeconds = intervalInMillSeconds;
        this.repeatCount = repeatCount;
    }

    @Override
    public Trigger makeTrigger() {
        return TriggerBuilder
            .newTrigger()
            .withIdentity(jobName, jobGroup)
            .withSchedule(SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMilliseconds(intervalInMillSeconds)
                .withRepeatCount(repeatCount - 1))
            .build();
    }

}
