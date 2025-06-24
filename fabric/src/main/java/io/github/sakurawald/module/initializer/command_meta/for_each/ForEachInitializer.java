package io.github.sakurawald.module.initializer.command_meta.for_each;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

@Document("""
    Provides `/foreach` command.
    If a command is only targets on single player, you can use `/foreach` to apply it for each online player.
    """)
public class ForEachInitializer extends ModuleInitializer {

    @Document("Execute a command targeted on single-player for each player online.")
    @CommandNode("foreach")
    @CommandRequirement(level = 4)
    private static int foreach(GreedyString rest) {
        String $rest = rest.getValue();

        for (ServerPlayerEntity player : ServerHelper.getPlayers()) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), $rest);
        }
        return CommandHelper.Return.SUCCESS;
    }
}
