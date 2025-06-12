package io.github.sakurawald.module.initializer.command_meta.run;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@CommandNode("run")
@CommandRequirement(level = 4)
public class RunInitializer extends ModuleInitializer {

    @CommandNode("as console")
    @Document("Execute a command as console.")
    private static int runAsConsole(@CommandSource ServerCommandSource source, GreedyString rest) {
        CommandExecutor.execute(ExtendedCommandSource.asConsole(source), rest.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("as player")
    @Document("Execute a command as a player.")
    private static int runAsPlayer(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString rest) {
        return CommandExecutor.execute(ExtendedCommandSource.asPlayer(source, player), rest.getValue());
    }

    @CommandNode("as fake-op")
    @Document("Execute a command as a player with fake-op.")
    private static int runAsFakeOp(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString rest) {
        return CommandExecutor.execute(ExtendedCommandSource.asFakeOp(source, player), rest.getValue());
    }
}
