package io.github.sakurawald.fuji.module.initializer.command_scheduler;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.command.argument.wrapper.JobName;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.config.model.CommandSchedulerConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.gui.JobGui;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.job.CommandScheduleJob;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.structure.Job;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quartz.JobDataMap;

import java.util.List;


@Document(id = 1751826754641L, value = """
    This module allows you to define `jobs` using `cron` language.
    To execute commands at schedule.
    """)
@ColorBox(id = 1751870574475L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?

    A `job` is used to execute commands periodically.
    We use `cron` language to define when the `job` should be `triggered`.
    """)
@ColorBox(id = 1751972254866L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ You can use `cron expression` generator, to specify when the job should be triggered.
    See https://www.freeformatter.com/cron-expression-generator-quartz.html

    Issue `/fuji inspect jobs` to see the `fire dates` of defined `jobs`.
    """)
@TestCase(action = "Issue `/fuji reload` command.", targets = "The jobs from command_scheduler module should be re-scheduled.")


@CommandNode("command-scheduler")
@CommandRequirement(level = 4)
public class CommandSchedulerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandSchedulerConfigModel> scheduler = new ObjectConfigurationHandler<>("scheduler.json", CommandSchedulerConfigModel.class);

    @Override
    protected void onInitialize() {
        reloadJobs();
    }

    @Override
    protected void onReload() {
        reloadJobs();
    }

    @Document(id = 1751826757048L, value = "List all defined jobs.")
    @CommandNode("list")
    private static int $list(@CommandSource ServerPlayerEntity player) {
        List<Job> jobs = scheduler.model().jobs;
        new JobGui(player, jobs, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826758887L, value = "Trigger a job manually.")
    @CommandNode("trigger")
    private static int $trigger(JobName jobName) {
        scheduler.model().jobs.stream()
            .filter(it -> it.getName().equals(jobName.getValue()))
            .findFirst()
            .ifPresent(Job::tryTrigger);

        return CommandHelper.Return.SUCCESS;
    }

    private void reloadJobs() {
        /* Un-schedule jobs. */
        LogUtil.info("Un-schedule jobs.");
        Managers.getScheduleManager().deleteJobs(CommandScheduleJob.class);

        /* Schedule jobs. */
        scheduler.model().jobs.forEach(definedJob -> {
            definedJob
                .getSchedules()
                .forEach(cron -> {
                    CommandScheduleJob job = new CommandScheduleJob(definedJob.getName(), new JobDataMap() {
                        {
                            this.put("job", definedJob);
                        }
                    }, () -> cron);
                    Managers.getScheduleManager().scheduleJob(job);
                });

            LogUtil.info("Schedule job -> {}", definedJob.getName());
        });
    }

}

