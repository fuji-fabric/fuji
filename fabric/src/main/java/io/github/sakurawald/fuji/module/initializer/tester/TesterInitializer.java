package io.github.sakurawald.fuji.module.initializer.tester;


import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.tester.functions.TestFunctions;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Date;
import java.util.List;
import java.util.Set;


@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    public static int x = ModuleManager.evalOnEnable(()->3);

    @SneakyThrows(Exception.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerPlayerEntity player) {

        Scheduler scheduler = Managers.getScheduleManager().getScheduler();

        List<String> jobGroupNames = scheduler.getJobGroupNames();
        jobGroupNames.forEach(it -> LogUtil.info("jobGroupName = {}", it));

        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
        jobKeys.forEach(it -> {
            LogUtil.info("jobKey = {}", it);

            try {
                List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(it);

                triggersOfJob.forEach(trigger -> {
                    LogUtil.info("trigger = {}", trigger);
                    Date nextFireTime = trigger.getNextFireTime();
                    LogUtil.info("nextFireTime = {}", nextFireTime);
                });

            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }

        });

        List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
        LogUtil.info("triggerGroupNames = {}", triggerGroupNames);
        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyGroup());
        LogUtil.info("triggerKeys = {}", triggerKeys);
//        scheduler.trigger

        return 0;
    }

    @CommandNode("text-replace")
    private static int testTextReplace(@CommandSource ServerPlayerEntity player) {
        TestFunctions.testTextReplacement(player);
        return 1;
    }

    @CommandNode("$1 minus $2")
    private static int $argumentReference(@CommandSource ServerPlayerEntity player, Integer a, Integer b) {
        player.sendMessage(Text.of(String.valueOf(a - b)));
        return 1;
    }

    @CommandNode("ctx")
    private static int $ctx(@CommandSource CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendMessage(Text.of("root"));
        return 1;
    }

    @CommandNode
    private static int $root(@CommandSource ServerPlayerEntity player) {
        player.sendMessage(Text.of("root"));
        return 1;
    }
}
