package mod.fuji.module.initializer.command_meta.delay;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.command.argument.wrapper.impl.GreedyCommandString;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStoppingEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Document(id = 1751969384267L, value = """
    This module provides the `/delay` command.
    To `delay` the `execution` of a specified command.
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
    private static int $delay(@CommandSource CommandSourceStack source, double time, GreedyCommandString rest) {
        String $rest = rest.getValue();

        long scheduleTimeMs = (long) (1000 * time);
        var unused = DELAY_COMMAND_EXECUTOR
            .schedule(() -> ServerHelper.executeSync(() -> CommandExecutor.executeSingle(ExtendedCommandSource.asConsole(source), $rest))
                , scheduleTimeMs
                , TimeUnit.MILLISECONDS);

        return CommandHelper.Return.SUCCESS;
    }


    @EventConsumer
    private static void resetDelaySchedulerExecutor(@Unused ServerStartedEvent event) {
        DELAY_COMMAND_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    }

    @EventConsumer
    private static void shutdownDelaySchedulerExecutor(@Unused ServerStoppingEvent event) {
        if (DELAY_COMMAND_EXECUTOR != null) {
            DELAY_COMMAND_EXECUTOR.shutdown();
        }
    }

}
