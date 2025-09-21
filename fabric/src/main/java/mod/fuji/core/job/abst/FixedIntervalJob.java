package mod.fuji.core.job.abst;

import lombok.NoArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@NoArgsConstructor
public abstract class FixedIntervalJob extends BaseJob {

    public static final int REPEAT_INDEFINITELY = -1;
    private int intervalInMillSeconds;
    private int repeatCount;

    public FixedIntervalJob(String jobGroup, String jobName, JobDataMap jobDataMap, int intervalInMillSeconds, int repeatCount) {
        super(jobGroup, jobName, jobDataMap, false);
        this.intervalInMillSeconds = intervalInMillSeconds;
        this.repeatCount = repeatCount;
    }

    @Override
    public Trigger makeTrigger() {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder
            .newTrigger()
            .withIdentity(jobName, jobGroup);

        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
            .simpleSchedule()
            .withIntervalInMilliseconds(this.intervalInMillSeconds);

        if (this.repeatCount == REPEAT_INDEFINITELY) {
            scheduleBuilder.repeatForever();
        } else {
            scheduleBuilder.withRepeatCount(repeatCount - 1);
        }

        return triggerBuilder
            .withSchedule(scheduleBuilder)
            .build();
    }

}
