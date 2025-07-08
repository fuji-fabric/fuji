package io.github.sakurawald.fuji.module.initializer.command_meta.run;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
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

@Document(id = 1751823988812L, value = """
    Provides `/run` command, to run a command with context.
    """)
@ColorBox(id = 1751968631536L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    Give random amount of diamonds to online players.
    Issue: `/run as console give @a minecraft:diamond %fuji:random 8 32%`
    """)
@ColorBox(id = 1751968672241L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    Give online players random amount of diamonds.
    Issue: `/run as console foreach give %fuji:escape player:name% minecraft:diamond %fuji:escape fuji:random 8 32 1%`
    """)
@ColorBox(id = 1751968707088L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    Execute a command as a specified player.
    Issue: `/run as player \\<player\\> back`
    """)
@ColorBox(id = 1751968753602L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    Execute a command as a fake-op.
    Issue: `/run as fake-op \\<player\\> give %player:name% minecraft:apple 1`
    """)



@CommandNode("run")
@CommandRequirement(level = 4)
public class RunInitializer extends ModuleInitializer {

    @Document(id = 1751823993461L, value = "Execute a command as console.")
    @CommandNode("as console")
    private static int runAsConsole(@CommandSource ServerCommandSource source, GreedyString rest) {
        CommandExecutor.execute(ExtendedCommandSource.asConsole(source), rest.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751823999061L, value = "Execute a command as a player.")
    @CommandNode("as player")
    private static int runAsPlayer(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString rest) {
        return CommandExecutor.execute(ExtendedCommandSource.asPlayer(source, player), rest.getValue());
    }

    @Document(id = 1751824003937L, value = "Execute a command as a player with fake-op.")
    @CommandNode("as fake-op")
    private static int runAsFakeOp(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString rest) {
        return CommandExecutor.execute(ExtendedCommandSource.asFakeOp(source, player), rest.getValue());
    }
}
