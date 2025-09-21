package mod.fuji.core.job.abst;

import mod.fuji.core.auxiliary.RandomUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

// NOTE: The no args constructor is only used for quartz to create the job instance.
@NoArgsConstructor
@Getter
public abstract class BaseJob implements Job {

    protected String jobGroup;
    protected String jobName;
    private JobDetail jobDetail;
    private TriggerKey triggerKey;
    protected boolean rescheduleAble;

    public BaseJob(@Nullable String jobGroup, @Nullable String jobName, @Nullable JobDataMap jobDataMap, boolean rescheduleAble) {
        /* Generate the value if null. */
        if (jobGroup == null) {
            jobGroup = this.getClass().getName();
        }
        if (jobName == null) {
            jobName = RandomUtil.randomUUID();
        }

        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }

        /* Initialize the variables. */
        this.jobGroup = jobGroup;
        this.jobName = jobName;
        // Job Detail = Job Class + Job Key + Job Data
        this.jobDetail = JobBuilder
            .newJob(this.getClass())
            .withIdentity(jobName, jobGroup)
            .usingJobData(jobDataMap)
            .build();
        this.triggerKey = new TriggerKey(jobName, jobGroup);
        this.rescheduleAble = rescheduleAble;
    }

    public abstract Trigger makeTrigger();

    @Override
    public String toString() {
        return "{jobGroup = %s, jobName = %s}".formatted(jobGroup, jobName);
    }
}
