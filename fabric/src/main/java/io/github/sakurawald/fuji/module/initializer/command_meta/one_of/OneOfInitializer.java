package io.github.sakurawald.fuji.module.initializer.command_meta.one_of;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.List;

@Document(id = 1751824713488L, value = """
    Provides `/one-of` command, to pick a random command from commands, and execute it.
    """)
@ColorBox(id = 1751968881805L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Pick a lucky command from specified command list, and then execute it.
    Issue: `/one-of say 1 one-of say 2 one-of say lucky %player:name%`
    """)

public class OneOfInitializer extends ModuleInitializer {

    @Document(id = 1751824718640L, value = "One-of command randomly pick one of commands and execute it as console.")
    @CommandNode("one-of")
    @CommandRequirement(level = 4)
    private static int $oneOf(@CommandSource ServerCommandSource source, GreedyString rest) {
        String $rest = rest.getValue();

        List<String> commands = Arrays.stream($rest.split("one-of"))
            .toList();

        String luckyCommand = RandomUtil
            .drawList(commands)
            .trim();

        LogUtil.debug("For {}, we pick the command {} to execute.", commands, luckyCommand);
        CommandExecutor.executeSingle(ExtendedCommandSource.fromSource(source), luckyCommand);
        return CommandHelper.Return.SUCCESS;
    }
}
