package io.github.sakurawald.fuji.module.initializer.command_meta.delay;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Document(id = 1751969384267L, value = """
    This module provides the `/delay` command.
    To allow you `delay` the `execution time` of a specified command.
    """)
@ColorBox(id = 1751870419626L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Only use `/delay` to perform short-term job.
    The `delayed commands` will not be persisted, if the server get a re-start.
    """)
@ColorBox(id = 1751969752045L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ A basic usage.
    Issue: `/delay 3 say three seconds passed`

    ◉ A nested delay.
    Issue: `/delay 1 delay 2 delay 3 say 6 seconds passed`

    ◉ A fine-grained delay.
    Issue: `/delay 3.1415926 say pi seconds passed`
    """)

public class DelayInitializer extends ModuleInitializer {

    private static ScheduledExecutorService DELAY_COMMAND_EXECUTOR;

    @Document(id = 1751824706971L, value = "Execute a command in seconds.")
    @CommandNode("delay")
    @CommandRequirement(level = 4)
    private static int $delay(@CommandSource ServerCommandSource source, double time, GreedyString rest) {
        String $rest = rest.getValue();

        long scheduleTimeMs = (long) (1000 * time);
        var unused = DELAY_COMMAND_EXECUTOR
            .schedule(() -> ServerHelper.executeSync(() -> CommandExecutor.execute(ExtendedCommandSource.asConsole(source), $rest))
                , scheduleTimeMs
                , TimeUnit.MILLISECONDS);

        return CommandHelper.Return.SUCCESS;
    }

    private static void resetDelaySchedulerExecutor() {
        DELAY_COMMAND_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    }

    private static void shutdownDelaySchedulerExecutor() {
        if (DELAY_COMMAND_EXECUTOR != null) {
            DELAY_COMMAND_EXECUTOR.shutdown();
        }
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> resetDelaySchedulerExecutor());
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> shutdownDelaySchedulerExecutor());
    }
}
