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
import io.github.sakurawald.fuji.module.initializer.command_scheduler.structure.CommandSchedulerJobDescriptor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quartz.JobDataMap;

import java.util.List;


@Document(id = 1755407283186L, value = """
    This module allows you to execute commands on a schedule.

    Typical use cases:
    1. Send broadcast messages at scheduled times.
    2. Execute a specified group of commands at scheduled times.
    """)
@ColorBox(id = 1755407830073L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    1. Define a `job` to execute `commands` on a schedule.
    1.a. The `schedule` is expressed using the `cron expression` language.
    1.b. You can specify multiple `cron expressions` for a `job`.
    1.c. A `job` is `triggered` if any of its `cron expressions` match.
    2. A `job` is automatically `triggered` according to its `cron expressions`.
    3. When a `job` is `triggered`, it will do:
    3.a. If the `enable` property is `false`, then do nothing.
    3.b. If the `remaining runs` property is `<= 0`, then do nothing.
    3.c. Otherwise, it decreases the `remaining runs` property by 1, and pick a random `command group` to execute.
    4. You can `trigger` a `job` using `/command-scheduler trigger \\<job\\>` manually.
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
        List<CommandSchedulerJobDescriptor> jobs = scheduler.model().jobs;
        new JobGui(player, jobs, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826758887L, value = "Trigger a job manually.")
    @CommandNode("trigger")
    private static int $trigger(JobName jobName) {
        scheduler.model().jobs.stream()
            .filter(it -> it.getName().equals(jobName.getValue()))
            .findFirst()
            .ifPresent(CommandSchedulerJobDescriptor::tryTrigger);

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

