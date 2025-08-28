package io.github.sakurawald.fuji.module.initializer.command_meta.OR;

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

@Document(id = 1756384425927L, value = """
    Provides the `/OR` command, which allows composing the return values of multiple commands.
    """)
@ColorBox(id = 1756385060343L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Compose the return values of multiple commands.
    Issue: `/OR \\<command-1\\> OR \\<command-2\\> OR \\<command-3\\> OR ...`

    ◉ Returns `SUCCESS` if the player has `iron_ingot x 8` or `gold_ingot x 4`
    Issue: `/OR has-item? Steve minecraft:iron_ingot 8 OR has-item? Steve minecraft:gold_ingot 4`
    """)
public class OrInitializer extends ModuleInitializer {

    @Document(id = 1756384432603L, value = "Returns `SUCCESS` if and only if `any of` the commands returns `SUCCESS`.")
    @CommandNode("OR")
    @CommandRequirement(level = 4)
    private static int $or(@CommandSource ServerCommandSource source, GreedyString rest) {
        String $rest = rest.getValue();
        List<String> commands = Arrays
            .stream($rest.split("OR"))
            .map(String::trim)
            .toList();

        ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.fromSource(source);
        List<Integer> returnValues = CommandExecutor.executeBatch(extendedCommandSource, commands);

        if (returnValues.stream().anyMatch(CommandHelper.Return::isSuccess)) {
            return CommandHelper.Return.SUCCESS;
        } else {
            return CommandHelper.Return.FAILURE;
        }
    }
}
