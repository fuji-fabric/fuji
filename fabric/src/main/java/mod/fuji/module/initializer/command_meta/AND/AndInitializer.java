package mod.fuji.module.initializer.command_meta.AND;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import java.util.Arrays;
import java.util.List;
import mod.fuji.module.initializer.command_meta.AND.command.argument.wrapper.AndGreedyCommandString;
import net.minecraft.commands.CommandSourceStack;

@Document(id = 1756383926802L, value = """
    Provides the `/AND` command, which allows composing the return values of multiple commands.
    """)
@ColorBox(id = 1756384526884L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Compose the return values of multiple commands.
    Issue: `/AND \\<command-1\\> AND \\<command-2\\> AND \\<command-3\\> AND ...`

    ◉ Returns `SUCCESS` if the player has `iron_ingot x 8` and `gold_ingot x 4`
    Issue: `/AND has-item? Steve minecraft:iron_ingot 8 AND has-item? Steve minecraft:gold_ingot 4`
    """)
public class AndInitializer extends ModuleInitializer {

    @Document(id = 1756383929301L, value = "Returns `SUCCESS` if and only if `all of` the commands returns `SUCCESS`.")
    @CommandNode("AND")
    @CommandRequirement(level = 4)
    private static int $and(@CommandSource CommandSourceStack source, AndGreedyCommandString rest) {
        String $rest = rest.getValue();
        List<String> commands = Arrays
            .stream($rest.split("AND"))
            .map(String::trim)
            .toList();

        ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.fromSource(source);
        List<Integer> returnValues = CommandExecutor.executeBatch(extendedCommandSource, commands);

        if (returnValues.stream().allMatch(CommandHelper.Return::isSuccess)) {
            return CommandHelper.Return.SUCCESS;
        } else {
            return CommandHelper.Return.FAILURE;
        }
    }
}
