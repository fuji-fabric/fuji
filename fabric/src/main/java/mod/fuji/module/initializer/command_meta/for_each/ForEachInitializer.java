package mod.fuji.module.initializer.command_meta.for_each;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.command.argument.wrapper.impl.GreedyCommandString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.level.ServerPlayer;

@Document(id = 1751823973159L, value = """
    This module provides the `/foreach` command.
    It allows applying a command that normally targets a single player `to each` online player.
    """)
@ColorBox(id = 1751968810100L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Say hello to online players.
    Issue: `/foreach say hello %player:name%`
    """)
public class ForEachInitializer extends ModuleInitializer {

    @Document(id = 1751823980406L, value = "Execute a command targeted on single-player for each player online.")
    @CommandNode("foreach")
    @CommandRequirement(level = 4)
    private static int $foreach(GreedyCommandString rest) {
        String $rest = rest.getValue();

        for (ServerPlayer player : PlayerHelper.Lookup.getOnlinePlayers()) {
            CommandExecutor.executeSingle(ExtendedCommandSource.asConsole(player.createCommandSourceStack()), $rest);
        }
        return CommandHelper.Return.SUCCESS;
    }
}
