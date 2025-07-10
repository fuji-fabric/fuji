package io.github.sakurawald.fuji.module.initializer.command_meta.chain;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Document(id = 1751824730196L, value = """
    Provides `/chain` command.
    It allows you to run another 2 commands.
    The first command is `any command`.
    The second command is `the chain command`.
    """)
@ColorBox(id = 1751870431402L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    In vanilla Minecraft, the return value of a command, is a `integer`.
    If integer less than zero, it's `failed`.
    If integer equals zero, it's `passed`.
    If integer grater than zero, it's `success`.
    """)
@ColorBox(id = 1751969034903L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ A nested chain
    Issue: `/chain say 1 chain say 2 chain say 3`

    ◉ A breakable chain
    Issue: `/chain bad command here chain say 2`

    ◉ Use chain command with predicate command
    Issue: `/run as player \\<player\\> chain test-level-perm %player:name% 4 chain say value is true`
    """)
public class ChainInitializer extends ModuleInitializer {
    private static final Pattern CHAIN_COMMAND_PARSER = Pattern.compile("(.+?)\\s+(chain .+)");

    @Document(id = 1751824736793L, value = "Chain commands and executes them in sequence, the chain will break if the previous one command fails.")
    @CommandNode("chain")
    @CommandRequirement(level = 4)
    private static int $chain(@CommandSource ServerCommandSource source, GreedyString rest) {
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
