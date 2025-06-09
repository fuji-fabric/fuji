package io.github.sakurawald.core.job.abst;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.manager.Managers;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class BaseJob implements Job {
    private static final Set<BaseJob> reschedulableJobs = new HashSet<>();

    protected boolean reschedulable = true;
    protected String jobGroup = null;
    protected String jobName = null;
    protected JobDetail jobDetail = null;
    protected TriggerKey triggerKey = null;

    // note: the no arguments constructor is only used for quartz to create and use the `execute` closure
    @SuppressWarnings("unused")
    public BaseJob() {
    }

    public BaseJob(String jobGroup, String jobName, JobDataMap jobDataMap) {
        if (jobGroup == null) {
            jobGroup = this.getClass().getName();
        }
        if (jobName == null) {
            jobName = UUID.randomUUID().toString();
        }

        // note: since quartz will construct AbstractJob using NoArgsConstructor, and all the AbstractJob instances used for `execute closure` have no jobDataMap.
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }

        this.jobGroup = jobGroup;
        this.jobName = jobName;

        this.jobDetail = JobBuilder.newJob(this.getClass()).withIdentity(jobName, jobGroup).usingJobData(jobDataMap).build();
        this.triggerKey = new TriggerKey(jobName, jobGroup);
    }

    public static void rescheduleAll() {
        for (BaseJob reschedulableJob : reschedulableJobs) {
            reschedulableJob.reschedule();
        }
    }

    public abstract Trigger makeTrigger();

    public void schedule() {
        try {
            LogUtil.debug("schedule job -> {}", this);
            Managers.getScheduleManager().scheduleJob(this.jobDetail, this.makeTrigger());

            if (this.reschedulable) {
                reschedulableJobs.add(this);
            }
        } catch (SchedulerException e) {
            LogUtil.error("failed to schedule job: exception = {}, job = {}", e, this);
        }
    }

    public void reschedule() {
        try {
            LogUtil.debug("re-schedule job -> {}", this);
            Managers.getScheduleManager().rescheduleJob(this.triggerKey, this.makeTrigger());
        } catch (SchedulerException e) {
            LogUtil.error("failed to reschedule job: exception = {}, job = {}", e, this);
        }
    }

    @Override
    public String toString() {
        return "{jobGroup = %s, jobName = %s}".formatted(jobGroup, jobName);
    }
}
