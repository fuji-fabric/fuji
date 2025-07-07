package io.github.sakurawald.fuji.module.initializer.command_debug;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Objects;

@Document(id = 1751827007525L, value = """
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
