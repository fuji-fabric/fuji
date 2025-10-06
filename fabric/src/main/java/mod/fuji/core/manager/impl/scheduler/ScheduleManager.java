package mod.fuji.core.manager.impl.scheduler;


import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.ExceptionUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.config.Configs;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStoppingEvent;
import mod.fuji.core.job.abst.BaseJob;
import mod.fuji.core.manager.abst.BaseManager;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@TestCase(action = "Issue `/stop` in the production environment.", targets = "The program should be terminated.")
public class ScheduleManager extends BaseManager {

    public static final String CRON_EVERY_SECOND = "* * * ? * *";
    public static final String CRON_EVERY_FIVE_SECONDS = "0/5 * * ? * * *";
    public static final String CRON_EVERY_TEN_SECONDS = "0/10 * * ? * * *";
    public static final String CRON_EVERY_MINUTE = "0 * * ? * * *";
    public static final String CRON_EVERY_THREE_MINUTES = "0 */3 * ? * *";
    public static final String CRON_EVERY_FIVE_MINUTES = "0 */5 * ? * *";

    private static final Set<BaseJob> STATIC_JOBS = new HashSet<>();

    @Getter
    private static Scheduler scheduler;

    static {
        /* Set logger level for quartz. */
        Level level = Level.getLevel(Configs.MAIN_CONTROL_CONFIG.model().core.scheduler.logger_level);
        Configurator.setAllLevels("org.quartz", level);

        // NOTE: Initialize the scheduler if needed, to prevent NPE while calling client-side entrypoints.
        getOrInitializeScheduler();
    }

    private static void getOrInitializeScheduler() {
        try {
            // NOTE: The scheduled jobs are associated with the scheduler ID, not the scheduler instance.
            // A job is stored in JobStore, not the Scheduler instance itself.
            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
            scheduler = stdSchedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            throw ExceptionUtil.makeReThrownException(e);
        }
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void startScheduler(@Unused ServerStartedEvent event) {
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            LogUtil.error("Failed to start the scheduler.", e);
        }
    }

    @EventConsumer
    private static void shutdownScheduler(@Unused ServerStoppingEvent event) {
        try {
            // NOTE: The shutdown method will return immediately, the executing jobs will continue running to completion.
            scheduler.shutdown(false);

            // NOTE: Make a new scheduler at once, after shutdown the old one. To prevent NPE when the integrated server re-started.
            if (ServerHelper.Environment.isClientSideIntegratedServer()) {
                getOrInitializeScheduler();
            }

        } catch (SchedulerException e) {
            LogUtil.error("Failed to shutdown the scheduler", e);
        }
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
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to add job: jobDetail = {}, trigger = {}",  jobDetail, trigger, e);
        }
    }

    public static void deleteJobs(@NotNull Class<?> jobGroupClass) {
        @NotNull String jobGroupName = jobGroupClass.getName();
        List<JobKey> jobKeys = new ArrayList<>(ScheduleManager.getJobKeys(jobGroupName));
        ScheduleManager.deleteJobs(jobKeys);
    }

    private static void deleteJobs(@NotNull List<JobKey> jobKeys) {
        try {
            LogUtil.debug("Delete jobs: jobKeys = {}", jobKeys);
            scheduler.deleteJobs(jobKeys);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to delete jobs: jobKeys = {}", jobKeys, e);
        }
    }

    private static @NotNull Set<JobKey> getJobKeys(@NotNull String jobGroup) {
        try {
            GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(jobGroup);
            return scheduler.getJobKeys(groupMatcher);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to get job keys: jobGroup = {}", jobGroup, e);
            return Collections.emptySet();
        }
    }

    public static void triggerJobs(ScheduleManager scheduleManager, @NotNull String jobGroup) {
        ScheduleManager.getJobKeys(jobGroup)
            .forEach(jobKey -> {
                try {
                    scheduler.triggerJob(jobKey);
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
            scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to update job triggers: triggerKey = {}, newTrigger = {}",  triggerKey, newTrigger, e);
        }
    }

    public static void reloadStaticJobTriggers() {
        STATIC_JOBS
            .forEach(ScheduleManager::updateJobTriggers);
    }

}
