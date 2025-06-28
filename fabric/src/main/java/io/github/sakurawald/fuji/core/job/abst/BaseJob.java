package io.github.sakurawald.fuji.core.job.abst;

import io.github.sakurawald.fuji.core.manager.Managers;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// NOTE: The no args constructor is only used for quartz to create the job instance.
@NoArgsConstructor
public abstract class BaseJob implements Job {

    private static final Set<BaseJob> RESCHEDULABLE_JOBS = new HashSet<>();

    protected String jobGroup;
    protected String jobName;
    protected JobDetail jobDetail;
    protected TriggerKey triggerKey;
    protected boolean canReschedule;

    public BaseJob(@Nullable String jobGroup, @Nullable String jobName, @Nullable JobDataMap jobDataMap, boolean canReschedule) {
        /* Generate the value if null. */
        if (jobGroup == null) {
            jobGroup = this.getClass().getName();
        }
        if (jobName == null) {
            jobName = UUID.randomUUID().toString();
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
        this.canReschedule = canReschedule;
    }

    public static void rescheduleAll() {
        RESCHEDULABLE_JOBS
            .forEach(BaseJob::reschedule);
    }

    public abstract Trigger makeTrigger();

    public void schedule() {
        Managers
            .getScheduleManager()
            .scheduleJob(this.jobDetail, this.makeTrigger());
        if (this.canReschedule) {
            RESCHEDULABLE_JOBS.add(this);
        }
    }

    public void reschedule() {
        Managers
            .getScheduleManager()
            .rescheduleJob(this.triggerKey, this.makeTrigger());
    }

    @Override
    public String toString() {
        return "{jobGroup = %s, jobName = %s}".formatted(jobGroup, jobName);
    }
}
