package io.github.sakurawald.module.initializer.command_meta.chain;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.core.structure.descriptor.annotation.ColorBox;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Document("""
    Provides `/chain` command.
    It allows you to run another 2 commands.
    The first command is any command.
    The second command is the chain command.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.NOTE, value = """
    In vanilla Minecraft, the return value of a command, is a `integer`.
    If integer less than zero, it's `failed`.
    If integer equals zero, it's `passed`.
    If integer grater than zero, it's `success`.
    """)


public class ChainInitializer extends ModuleInitializer {
    private static final Pattern CHAIN_COMMAND_PARSER = Pattern.compile("(.+?)\\s+(chain .+)");

    @Document("Chain commands and executes them in sequence, the chain will break if the previous one command fails.")
    @CommandNode("chain")
    @CommandRequirement(level = 4)
    private static int chain(@CommandSource ServerCommandSource source, GreedyString rest) {
        String $rest = rest.getValue();

        Matcher matcher = CHAIN_COMMAND_PARSER.matcher($rest);
        if (matcher.find()) {
            String first = matcher.group(1);
            String second = matcher.group(2);
            int value = CommandExecutor.execute(ExtendedCommandSource.fromSource(source), first);
            // Break chain, if command `fail`.
            if (value >= 0) {
                CommandExecutor.execute(ExtendedCommandSource.fromSource(source), second);
            }
        } else {
            CommandExecutor.execute(ExtendedCommandSource.fromSource(source), $rest);
        }

        return CommandHelper.Return.SUCCESS;
    }
}
