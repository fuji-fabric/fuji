package mod.fuji.module.initializer.command_debug;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1751827007525L, value = """
    This module provides debug tools for executing commands.
    """)
@ColorBox(id = 1751903540774L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Execute a specified command, and sees the debug info.
    Issue `/command-debug has-exp? Alice 100`
    """)
public class CommandDebugInitializer extends ModuleInitializer {

    @CommandNode("command-debug")
    @CommandRequirement(level = 4)
    private static int $debug(@CommandSource ServerCommandSource source, GreedyString command) throws CommandSyntaxException {
        String commandString = command.getValue();

        int returnValue = CommandHelper
            .getCommandDispatcher()
            .execute(commandString, source);

        TextHelper.sendTextByKey(source, "command.string", commandString);
        TextHelper.sendTextByKey(source, "command.source", source.getName());
        TextHelper.sendTextByKey(source, "command.return", returnValue);
        return CommandHelper.Return.SUCCESS;
    }
}
