package mod.fuji.core.job.abst;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.JobDataMap;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@NoArgsConstructor
public abstract class FixedIntervalJob extends BaseJob {

    public static final int REPEAT_INDEFINITELY = -1;
    private int intervalInMillSeconds;
    private int repeatCount;

    public FixedIntervalJob(@Nullable String jobGroup, @Nullable String jobName, @Nullable JobDataMap jobDataMap, int intervalInMillSeconds, int repeatCount) {
        super(jobGroup, jobName, jobDataMap, false);
        this.intervalInMillSeconds = intervalInMillSeconds;
        this.repeatCount = repeatCount;
    }

    @Override
    public @NotNull Trigger makeTrigger() {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder
            .newTrigger()
            .withIdentity(this.getTriggerKey());

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
