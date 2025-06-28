package io.github.sakurawald.fuji.module.initializer.command_meta.run;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document("""
    Provides `/run` command, to run a command with context.
    """)
@CommandNode("run")
@CommandRequirement(level = 4)
public class RunInitializer extends ModuleInitializer {

    @Document("Execute a command as console.")
    @CommandNode("as console")
    private static int runAsConsole(@CommandSource ServerCommandSource source, GreedyString rest) {
        CommandExecutor.execute(ExtendedCommandSource.asConsole(source), rest.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Execute a command as a player.")
    @CommandNode("as player")
    private static int runAsPlayer(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString rest) {
        return CommandExecutor.execute(ExtendedCommandSource.asPlayer(source, player), rest.getValue());
    }

    @Document("Execute a command as a player with fake-op.")
    @CommandNode("as fake-op")
    private static int runAsFakeOp(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString rest) {
        return CommandExecutor.execute(ExtendedCommandSource.asFakeOp(source, player), rest.getValue());
    }
}
