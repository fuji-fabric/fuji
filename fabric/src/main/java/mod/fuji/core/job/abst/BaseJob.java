package mod.fuji.core.job.abst;

import mod.fuji.core.auxiliary.RandomUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

// NOTE: The no args constructor is only used for quartz to create the job instance.
@NoArgsConstructor
public abstract class BaseJob implements Job {

    String jobGroup;
    String jobName;

    /**
 * Job Detail = Job Class + Job Key + Job Data
 **/
    @Getter
    JobDetail jobDetail;

    @Getter
    TriggerKey triggerKey;

    @Getter
    boolean staticJob;

    public BaseJob(@Nullable String jobGroup, @Nullable String jobName, @Nullable JobDataMap jobDataMap, boolean staticJob) {
        /* Initialize with sensible default values. */
        if (jobGroup == null) {
            jobGroup = this.getClass().getName();
        }
        if (jobName == null) {
            jobName = RandomUtil.drawUUID();
        }
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }

        /* Initialize the variables. */
        this.jobGroup = jobGroup;
        this.jobName = jobName;
        this.jobDetail = JobBuilder
            .newJob(this.getClass())
            .withIdentity(new JobKey(jobName, jobGroup))
            .usingJobData(jobDataMap)
            .build();
        // NOTE: For simplicity, use identity mapping from TriggerKey to JobKey.
        this.triggerKey = new TriggerKey(jobName, jobGroup);
        this.staticJob = staticJob;
    }

    public abstract @NotNull Trigger makeTrigger();

    @Override
    public String toString() {
        return "{jobGroup = %s, jobName = %s}".formatted(jobGroup, jobName);
    }
}
