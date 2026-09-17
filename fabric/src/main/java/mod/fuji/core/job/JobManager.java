package mod.fuji.core.job;


import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.Configs;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.job.abst.BaseJob;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@TestCase(action = "Issue `/stop` in the production environment.", targets = "The program should be terminated.")
public class JobManager {

    public static final String CRON_EVERY_SECOND = "* * * ? * *";
    public static final String CRON_EVERY_FIVE_SECONDS = "0/5 * * ? * * *";
    public static final String CRON_EVERY_TEN_SECONDS = "0/10 * * ? * * *";
    public static final String CRON_EVERY_MINUTE = "0 * * ? * * *";
    public static final String CRON_EVERY_THREE_MINUTES = "0 */3 * ? * *";
    public static final String CRON_EVERY_FIVE_MINUTES = "0 */5 * ? * *";
    private static final Set<BaseJob> STATIC_JOBS = new HashSet<>();

    static {
        /* Set logger level for quartz. */
        Level level = Level.getLevel(Configs.MAIN_CONTROL_CONFIG.model().core.scheduler.logger_level);
        Configurator.setAllLevels("org.quartz", level);
    }

    public static void addJob(@NotNull BaseJob baseJob) {
        JobDetail jobDetail = baseJob.getJobDetail();
        Trigger trigger = baseJob.makeTrigger();

        try {
            LogUtil.debug("Add job: jobDetail = {}, trigger = {}", jobDetail, trigger);

            /* Remember static jobs. */
            if (baseJob.isStaticJob()) {
                STATIC_JOBS.add(baseJob);
            }

            /* Add this job. */
            GlobalScheduler.getInstance().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to add job: jobDetail = {}, trigger = {}",  jobDetail, trigger, e);
        }
    }

    public static void deleteJobs(@NotNull Class<?> jobGroupClass) {
        @NotNull String jobGroupName = jobGroupClass.getName();
        List<JobKey> jobKeys = new ArrayList<>(JobManager.getJobKeys(jobGroupName));
        JobManager.deleteJobs(jobKeys);
    }

    private static void deleteJobs(@NotNull List<JobKey> jobKeys) {
        try {
            LogUtil.debug("Delete jobs: jobKeys = {}", jobKeys);
            GlobalScheduler.getInstance().deleteJobs(jobKeys);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to delete jobs: jobKeys = {}", jobKeys, e);
        }
    }

    private static @NotNull Set<JobKey> getJobKeys(@NotNull String jobGroup) {
        try {
            GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(jobGroup);
            return GlobalScheduler.getInstance().getJobKeys(groupMatcher);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to get job keys: jobGroup = {}", jobGroup, e);
            return Collections.emptySet();
        }
    }

    public static void triggerJobs(@NotNull String jobGroup) {
        JobManager.getJobKeys(jobGroup)
            .forEach(jobKey -> {
                try {
                    GlobalScheduler.getInstance().triggerJob(jobKey);
                } catch (SchedulerException e) {
                    LogUtil.error("Failed to trigger jobs: jobGroup = {}", jobGroup, e);
                }
            });
    }

    public static void updateJobTriggers(@NotNull BaseJob baseJob) {
        TriggerKey triggerKey = baseJob.getTriggerKey();
        Trigger newTrigger = baseJob.makeTrigger();
        try {
            LogUtil.debug("Update job triggers: triggerKey = {}, newTrigger = {}", triggerKey, newTrigger);
            GlobalScheduler.getInstance().rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to update job triggers: triggerKey = {}, newTrigger = {}",  triggerKey, newTrigger, e);
        }
    }

    public static void reloadStaticJobTriggers() {
        STATIC_JOBS
            .forEach(JobManager::updateJobTriggers);
    }

}
