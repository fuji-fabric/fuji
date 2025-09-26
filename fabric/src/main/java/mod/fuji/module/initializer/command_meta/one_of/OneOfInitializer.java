package mod.fuji.module.initializer.command_meta.one_of;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_meta.one_of.argument.wrapper.OneOfGreedyCommandString;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.List;

@Document(id = 1751824713488L, value = """
    Provides `/one-of` command, to pick a random command from specified commands, and execute it.
    """)
@ColorBox(id = 1751968881805L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Pick a lucky command from specified command list, and then execute it.
    Issue: `/one-of say 1 one-of say 2 one-of say lucky %player:name%`
    """)
public class OneOfInitializer extends ModuleInitializer {

    @Document(id = 1751824718640L, value = "One-of command randomly pick one of commands and execute it as console.")
    @CommandNode("one-of")
    @CommandRequirement(level = 4)
    private static int $oneOf(@CommandSource ServerCommandSource source, OneOfGreedyCommandString rest) {
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
