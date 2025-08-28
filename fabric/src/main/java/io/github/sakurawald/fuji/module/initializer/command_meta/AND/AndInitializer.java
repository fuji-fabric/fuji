package io.github.sakurawald.fuji.module.initializer.command_meta.AND;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;

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
    private static int $and(@CommandSource ServerCommandSource source, GreedyString rest) {
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
