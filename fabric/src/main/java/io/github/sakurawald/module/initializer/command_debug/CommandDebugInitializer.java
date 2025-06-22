package io.github.sakurawald.module.initializer.command_debug;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Objects;

@Document("""
    This module provides debug tools for executing commands.
    """)
public class CommandDebugInitializer extends ModuleInitializer {

    @CommandNode("command-debug")
    @CommandRequirement(level = 4)
    private static int $debug(@CommandSource ServerCommandSource source, GreedyString command) throws CommandSyntaxException {
        String commandString = command.getValue();

        int returnValue = Objects
            .requireNonNull(ServerHelper.getCommandDispatcher())
            .execute(commandString, source);

        LogUtil.info("""
            [Command Debug]
            Command String = {}
            Command Source = {}
            Command Return = {}
            """
            , commandString
            , source.getName()
            , returnValue
        );

        return CommandHelper.Return.SUCCESS;
    }
}
