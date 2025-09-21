package mod.fuji.module.initializer.command_meta.chain;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Document(id = 1751824730196L, value = """
    This module provides the `/chain` command, which allows executing `two commands` sequentially:
    1. The first command, is `any command`. (Required)
    2. The second command, is `/chain` command. (Optionally)
    """)
@ColorBox(id = 1751870431402L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ What is the purpose of `/chain` command?
    Its typical use-case is to `condense` two commands into one command.
    So that you can put `more than 1 command` in some places.
    For example: `/execute run chain say 1 chain say 2`

    ◉ The `return value` of a vanilla Minecraft command.
    In vanilla Minecraft, the `return value` of a command, is a `integer`.
    If integer less than zero, it's `failed`.
    If integer equals zero, it's `passed`.
    If integer grater than zero, it's `success`.

    <green>So, we can know whether a command was executed successfully or not, based on the return value of that command.

    ◉ How the `/chain` command works?
    The `/chain` command takes `exactly one argument`, whose type is `greedy string`.
    A `greedy string` argument type is always be `the last` argument of a command.
    Once the `command parser` sees a `greedy string` argument type, it knows this is `the last` argument.
    So the parser will `feed` the `greedy string argument` all the remaining characters.
    In other words, a `greedy string argument` will `eat` all the remaining characters that are still not be consumed by preceding arguments.

    For example, the `/say Hello Alice` also has exactly one argument, whose type is `greedy string`.
    You didn't need to use `"` character to escape the `space character`, this is because the argument type of `/say` is `greedy string`.

    Take `/chain say 1 chain say 2` as an example:
    The first `/chain` command will receive the `say 1 chain say 2` as the value of `its first and only argument`.
    Then the `/chain` command will split the value into two parts.
    The first part is `say 1`, and the `/chain` command will submit it to the `command executor`.
    The second part is `chain say 2`, and the `/chain` command will decide whether to submit it `based on` the return value of `the previous submitted command`.
    It's a recursive process.
    """)
@ColorBox(id = 1751969034903L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ A nested chain
    Issue: `/chain say 1 chain say 2 chain say 3`

    ◉ A breakable chain
    Issue: `/chain bad command here chain say 2`

    ◉ Use chain command with predicate command
    Issue: `/run as player Alice chain test-level-perm %player:name% 4 chain say value is true`

    ◉ Leverage the `/execute if` command.
    Issue: `/chain execute if block ~ ~-1 ~ minecraft:diamond_block if entity @s[nbt={Inventory:[{id:"minecraft:diamond"}]}] chain say You are standing on diamond block and have diamond.`
    <green>NOTE: This use-case is similar to `command_meta.if` module.
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

            int value = CommandExecutor.executeSingle(ExtendedCommandSource.fromSource(source), first);
            // Break chain, if command `fail`.
            if (CommandHelper.Return.isSuccess(value)) {
                CommandExecutor.executeSingle(ExtendedCommandSource.fromSource(source), second);
            }
        } else {
            CommandExecutor.executeSingle(ExtendedCommandSource.fromSource(source), $rest);
        }

        return CommandHelper.Return.SUCCESS;
    }
}
