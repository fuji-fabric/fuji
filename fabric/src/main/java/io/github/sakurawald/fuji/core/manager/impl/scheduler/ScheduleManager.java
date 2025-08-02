package io.github.sakurawald.fuji.core.manager.impl.scheduler;


import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.job.abst.BaseJob;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
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

@TestCase(steps = "Issue `/stop` in the production environment.", purposes = "The program should be terminated.")
@SuppressWarnings("LombokGetterMayBeUsed")
public class ScheduleManager extends BaseManager {

    public static final String CRON_EVERY_SECOND = "* * * ? * *";
    public static final String CRON_EVERY_MINUTE = "0 * * ? * * *";
    public static final String CRON_EVERY_FIVE_SECONDS = "0/5 * * ? * * *";
    public static final String CRON_EVERY_TEN_SECONDS = "0/10 * * ? * * *";

    private static final Set<BaseJob> RESCHEDULABLE_JOBS = new HashSet<>();

    @Getter
    private Scheduler scheduler;

    {
        /* Set logger level for quartz. */
        Level level = Level.getLevel(Configs.MAIN_CONTROL_CONFIG.model().core.scheduler.logger_level);
        Configurator.setAllLevels("org.quartz", level);

        // NOTE: Reset the scheduler for client-side, to prevent NPE.
        resetScheduler();
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> Managers.getScheduleManager().startScheduler());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> Managers.getScheduleManager().shutdownScheduler());
    }

    public void scheduleJob(BaseJob baseJob) {
        JobDetail jobDetail = baseJob.getJobDetail();
        Trigger trigger = baseJob.makeTrigger();

        try {
            LogUtil.debug("Schedule job: jobDetail = {}, trigger = {}", jobDetail, trigger);

            // Track the reschedule-able job.
            if (baseJob.isRescheduleAble()) {
                RESCHEDULABLE_JOBS.add(baseJob);
            }

            // Scheduler the job.
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to schedule job: jobDetail = {}, trigger = {}",  jobDetail, trigger, e);
        }
    }

    public void rescheduleJob(BaseJob baseJob) {
        TriggerKey triggerKey = baseJob.getTriggerKey();
        Trigger newTrigger = baseJob.makeTrigger();

        try {
            LogUtil.debug("Re-schedule job: triggerKey = {}, newTrigger = {}", triggerKey, newTrigger);
            this.scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to reschedule job: triggerKey = {}, newTrigger = {}",  triggerKey, newTrigger, e);
        }
    }

    public void rescheduleJobs() {
        RESCHEDULABLE_JOBS
            .forEach(this::rescheduleJob);
    }

    public void deleteJobs(Class<?> clazz) {
        List<JobKey> jobKeys = new ArrayList<>(this.getJobKeys(clazz.getName()));
        this.deleteJobs(jobKeys);
    }

    private void deleteJobs(List<JobKey> jobKeys) {
        try {
            LogUtil.debug("Delete job keys: {}", jobKeys);
            this.scheduler.deleteJobs(jobKeys);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to delete jobs: jobKeys = {}", jobKeys, e);
        }
    }

    private Set<JobKey> getJobKeys(@NotNull String jobGroup) {
        GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(jobGroup);
        try {
            return scheduler.getJobKeys(groupMatcher);
        } catch (SchedulerException e) {
            LogUtil.error("Failed to get job keys: jobGroup = {}", jobGroup, e);
        }
        return Collections.emptySet();
    }

    public void triggerJobs(@NotNull String jobGroup) {
        this.getJobKeys(jobGroup)
            .forEach(jobKey -> {
                try {
                    scheduler.triggerJob(jobKey);
                } catch (SchedulerException e) {
                    LogUtil.error("Failed to trigger jobs: jobGroup = {}", jobGroup, e);
                }
            });
    }

    private void resetScheduler() {
        try {
            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
            scheduler = stdSchedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    private void startScheduler() {
        /* Make a new scheduler. */
        resetScheduler();

        /* Start the scheduler. */
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            LogUtil.error("Failed to start the scheduler.", e);
        }
    }

    private void shutdownScheduler() {
        try {
            scheduler.shutdown(false);

            // NOTE: Make a new scheduler at once, after shutdown the old one. To prevent NPE in client-side environment.
            if (ServerHelper.isClientSideIntegratedServer()) {
                resetScheduler();
            }

        } catch (SchedulerException e) {
            LogUtil.error("Failed to shutdown the scheduler", e);
        }
    }
}
