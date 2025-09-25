package mod.fuji.module.initializer.command_meta.NOT;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyCommandString;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1756136221326L, value = """
    Provides a `/NOT <command>` command.
    To execute the specified command, and `reverse` the return values of `SUCCESS` and `FAILURE`.
    """)
@ColorBox(id = 1756136351403L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    1. Execute the `target command` as the command source, and get the `command return value`.
    1.a. If the `command return value` represents `SUCCESS` (value > 0), then return `FAILURE` (value 0).
    1.b. If the `command return value` represents `FAILURE` (value = 0), then return `SUCCESS` (value 1).
    """)
@ColorBox(id = 1756137411423L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use with other `predicate` commands.
    Issue: `/IF NOT has-item? \\<player\\> minecraft:apple 16 THEN say You don't have 16 apples. ELSE say You have 16 apples.`
    """)
public class NotInitializer extends ModuleInitializer {

    @Document(id = 1756136578216L, value = "Execute the command as the console, and reverse the return values of `SUCCESS` and `FAILURE`.")
    @CommandNode("NOT")
    @CommandRequirement(level = 4)
    private static int $not(@CommandSource ServerCommandSource source, GreedyCommandString command) {
        int commandReturnValue = CommandExecutor.executeSingle(ExtendedCommandSource.fromSource(source), command.getValue());

        if (CommandHelper.Return.isSuccess(commandReturnValue)) {
            return CommandHelper.Return.FAILURE;
        } else {
            return CommandHelper.Return.SUCCESS;
        }
    }

}
