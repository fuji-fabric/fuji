package io.github.sakurawald.fuji.module.initializer.command_meta.delay;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ColorBox(id = 1751870419626L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    Only use `/delay` to perform short-term job.
    The `delayed commands` will not be persisted, if the server get a re-start.
    """)

public class DelayInitializer extends ModuleInitializer {

    private static final ScheduledExecutorService DELAY_COMMAND_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    @Document(id = 1751824706971L, value = "Execute a command in seconds.")
    @CommandNode("delay")
    @CommandRequirement(level = 4)
    private static int delay(@CommandSource ServerCommandSource source, double time, GreedyString rest) {
        String $rest = rest.getValue();

        long scheduleTimeMs = (long) (1000 * time);
        DELAY_COMMAND_EXECUTOR
            .schedule(() -> ServerHelper.getServer().executeSync(() -> CommandExecutor.execute(ExtendedCommandSource.asConsole(source), $rest))
                , scheduleTimeMs
                , TimeUnit.MILLISECONDS);

        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> DELAY_COMMAND_EXECUTOR.shutdown());
    }
}
