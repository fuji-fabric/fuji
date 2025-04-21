package io.github.sakurawald.module.initializer.command_meta.delay;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayInitializer extends ModuleInitializer {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @CommandNode("delay")
    @CommandRequirement(level = 4)
    @Document("Execute a command in seconds.")
    private static int delay(@CommandSource ServerCommandSource source, double time, GreedyString rest) {
        String $rest = rest.getValue();

        long scheduleTimeMs = (long) (1000 * time);
        executor
            .schedule(() -> ServerHelper.getServer().executeSync(() -> CommandExecutor.execute(ExtendedCommandSource.asConsole(source), $rest))
                , scheduleTimeMs
                , TimeUnit.MILLISECONDS);

        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> executor.shutdown());
    }
}
