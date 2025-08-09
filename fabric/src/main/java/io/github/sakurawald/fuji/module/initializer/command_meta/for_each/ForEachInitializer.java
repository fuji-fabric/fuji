package io.github.sakurawald.fuji.module.initializer.command_meta.for_each;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751823973159L, value = """
    Provides `/foreach` command.
    If a command only targets a single player, you can use `/foreach` to apply it to each online player.
    """)
@ColorBox(id = 1751968810100L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Say hello to online players.
    Issue: `/foreach say hello %player:name%`
    """)
public class ForEachInitializer extends ModuleInitializer {

    @Document(id = 1751823980406L, value = "Execute a command targeted on single-player for each player online.")
    @CommandNode("foreach")
    @CommandRequirement(level = 4)
    private static int $foreach(GreedyString rest) {
        String $rest = rest.getValue();

        for (ServerPlayerEntity player : PlayerHelper.Lookup.getOnlinePlayers()) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), $rest);
        }
        return CommandHelper.Return.SUCCESS;
    }
}
