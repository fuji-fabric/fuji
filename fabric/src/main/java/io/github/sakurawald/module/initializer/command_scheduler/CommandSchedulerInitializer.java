package io.github.sakurawald.module.initializer.command_scheduler;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_scheduler.command.argument.wrapper.JobName;
import io.github.sakurawald.module.initializer.command_scheduler.config.model.CommandSchedulerConfigModel;
import io.github.sakurawald.module.initializer.command_scheduler.gui.JobGui;
import io.github.sakurawald.module.initializer.command_scheduler.job.CommandScheduleJob;
import io.github.sakurawald.module.initializer.command_scheduler.structure.Job;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quartz.JobDataMap;

import java.util.List;


@CommandNode("command-scheduler")
@CommandRequirement(level = 4)
public class CommandSchedulerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandSchedulerConfigModel> scheduler = new ObjectConfigurationHandler<>("scheduler.json", CommandSchedulerConfigModel.class)
        .autoSaveEveryMinute();

    @Override
    protected void onInitialize() {
        updateJobs();
    }

    @Override
    protected void onReload() {
        updateJobs();
    }

    @CommandNode("list")
    @Document("List all defined jobs.")
    private static int list(@CommandSource ServerPlayerEntity player) {
        List<Job> jobs = scheduler.model().jobs;
        new JobGui(player, jobs, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("trigger")
    @Document("Trigger a job manually.")
    private static int trigger(JobName jobName) {
        scheduler.model().jobs.stream()
            .filter(it -> it.getName().equals(jobName.getValue()))
            .findFirst()
            .ifPresent(Job::trigger);

        return CommandHelper.Return.SUCCESS;
    }

    private void updateJobs() {
        LogUtil.info("un-schedule jobs");
        Managers.getScheduleManager().deleteJobs(CommandScheduleJob.class);

        scheduler.model().jobs.forEach(scheduleJob -> {
            scheduleJob.getCrons().forEach(cron -> new CommandScheduleJob(new JobDataMap() {
                {
                    this.put("job", scheduleJob);
                }
            }, () -> cron).schedule());

            LogUtil.info("schedule job -> {}", scheduleJob.getName());
        });
    }

}

