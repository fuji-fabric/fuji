package io.github.sakurawald.fuji.module.initializer.command_meta.IF;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1753515891751L, value = """
    This module provides a simple `/IF` command.
    It can be used to express `conditional logic`, such as `IF ... THEN ... ELSE ...`.
    """)
@ColorBox(id = 1753515940860L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    The syntax of `/IF` command is: `/IF \\<condition-command\\> THEN \\<then-command\\> ELSE \\<else-command\\>`
    <red>The `IF`, `THEN`, `ELSE` literals must be `capitalized`.

    The execution flow of this command is as follows:
    1. Start the `execution` of a given `/IF` command instance.
    2. Check the possible `ambiguity` of the given `/IF` command instance.
    2.a. If `no ambiguity` found, goto step `3.`
    2.b. If there is `an ambiguity` found, abort the entire execution of `/IF` command.
    3. Execute the `condition-command`, and get its `return value`:
    3.a. If `the return value` is `true`, then `executes` the `then-command`.
    3.b. If `the return value` is `false`, then `executes` the `else-command`.

    ◉ Any `failure` will lead to the execution of `else-command`.
    <red>If there is any `error` during the `execution` of `condition-command`, the `else-command` will be executed.
    For example, the `/IF bad command THEN say true ELSE say false` command will do the following things:
    1. First, execute the `/bad command`.
    2. Due to the `error` during the execution of `/bad command`, the `/say false` command will be executed.

    ◉ `Recursive IF` is not supported.
    The `/IF` command is a `simple` enough command, to provide a very basic `conditional expression`.
    The `nested IF` is not supported.
    Once there is an `ambiguity` found in the `/IF` command, the entire execution of the `/IF` command will be aborted.
    """)
@ColorBox(id = 1753596414690L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The difference between `/IF` command and `/execute if` command.
    The `/execute if` command is a powerful command:
    1. You can use `recursive if`.
    For example: `/execute if \\<first-condition\\> if \\<second-condition\\>`
    2. You can use lots of `built-in Minecraft predicates`.
    For example: `/execute if {block|biome|loaded|dimension|score|blocks|entity|predicate|function|items|data}`
    3. You can `simulate` the `else-clause` by combining the `/execute if` and `/execute unless` commands.

    ◉ What is the `purpose` of `/IF` command?
    1. It allows you to use `any command` as the `condition`, based on the `return value` of that command.
    2. It prevents the `side-effect` from the `condition-clause` and `then-command`. (Without the `/tag` or `/scoreboard`)
    3. It provides a simple and in-line way to express the `IF ... THEN ... ELSE ...` logic.
    4. It supports the `placeholders`.
    5. Still, You can leverage the `/execute if` command as the `condition-command` of `/IF` command.
    """)
@ColorBox(id = 1753594904128L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Useful commands.
    1. https://minecraft.fandom.com/wiki/Commands/execute
    2. https://minecraft.wiki/w/Commands/return
    3. https://minecraft.fandom.com/wiki/Commands/scoreboard
    4. https://minecraft.fandom.com/wiki/Commands/tag
    5. https://minecraft.fandom.com/wiki/Commands/data
    """)
@ColorBox(id = 1753668342721L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Write the `IF ... THEN ... ELSE ...` logic conveniently.
    You can use `command_bundle` module to create a `template command` to help you write `conditional logic`.
    For example, you can create a new command `/try-give-item <item> <required-balance>`.
    """)
@ColorBox(id = 1753521193815L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Check whether a player is an `operator`.
    Issue: `/IF is-op? %player:name% THEN say It is op. ELSE say It is not op.`

    ◉ Check whether a player has sufficient balance.
    Issue: `/IF has-currency? %player:name% fuji:gold 100 THEN say Sufficient balance. ELSE say Insufficient balance.`

    ◉ Use a shorten version of `/IF ... THEN ...` command.
    The `/nop` command `performs no action` and always `returns success`.
    You can use it as a `dummy command` in the `else-command` place, to effectively ignore it.
    Issue: `/IF is-op? %player:name% THEN say It is op. ELSE nop`

    ◉ Specify `multiple commands` in one place, by using the `/chain` command.
    Issue: `/IF is-op? %player:name% THEN chain say It is op. chain say Hello op! ELSE nop`

    ◉ Leverage the power of `/execute if`. (And the `/execute unless` command)
    Issue: `/IF execute if block ~ ~-1 ~ minecraft:diamond_block THEN say You are standing on diamond block. ELSE say You are not standing on diamond block.`

    ◉ Combine 2 conditions using `chained if`, and only execute the `then-command` when they are all `true`.
    Issue: `/IF execute if block ~ ~-1 ~ minecraft:diamond_block if entity @s[nbt={Inventory:[{id:"minecraft:diamond"}]}] THEN say All true. ELSE say Not all true.`
    The `then-command` will only be executed if the `condition-command` is `true`:
    1. The player is standing on a `minecraft:diamond_block` block.
    2. The player has a `minecraft:diamond` item in its inventory.
    """)
@ColorBox(id = 1753592396075L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Use `/command-debug` command to see the `return value` of a command.
    The `return value` is not displayed explicitly in-game.
    However, you can use `/command-debug` command to see `the return value` of a specified command.
    Issue:
    1. `/command-debug execute if block ~ ~-1 ~ minecraft:diamond_block`
    2. `/command-debug execute if block ~ ~-1 ~ minecraft:diamond_block if entity @s[nbt={Inventory:[{id:"minecraft:diamond"}]}]`

    ◉ The behaviour of `/execute if` command.
    1. When `none condition` is `true`, it throws a `Test failed` exception.
    2. When `one of the specified conditions` is `true`, it will returns `0` to mean `partial success`.
    3. When `all of the specified conditions` are `true`, it will returns `a positive integer` to mean `success`.
    NOTE: This `positive integer` equals to `the number of successful branch` if the `/execute if` command is `forked`.

    For example, if you have `3` players online, but only `2` of them are standing on a `minecraft:diamond_block` block.
    Issue: `/command-debug execute at @a if block ~ ~-1 ~ minecraft:diamond_block`
    You will get the integer `2` as the `return value` of that `/execute ...` command instance.
    """)
public class IfInitializer extends ModuleInitializer {

    @TestCase(action = "Issue `/IF execute if block ~ ~-1 ~ minecraft:diamond_block THEN say You are standing on diamond block. ELSE say You are not standing on diamond block.` command.", targets = {
        "You should not see the red `Test failed` in the feedback."
    })
    private static final String CONDITIONAL_FAIL_EXCEPTION_PREFIX_STRING = "Test failed";
    private static final Pattern IF_COMMAND_PARSER = Pattern.compile("(.+)\\s+THEN\\s+(.+)\\s+ELSE\\s+(.+)");
    private static final String THEN_LITERAL = "THEN";
    private static final String ELSE_LITERAL = "ELSE";

    private static void checkAmbiguity(ServerCommandSource source, String conditionCommand, String thenCommand, String elseCommand) {
        if (conditionCommand.contains(THEN_LITERAL) || conditionCommand.contains(ELSE_LITERAL)) {
            TextHelper.sendTextByKey(source, "if.matcher.ambiguity");
            throw new AbortCommandExecutionException();
        }

        if (thenCommand.contains(THEN_LITERAL) || thenCommand.contains(ELSE_LITERAL)) {
            TextHelper.sendTextByKey(source, "if.matcher.ambiguity");
            throw new AbortCommandExecutionException();
        }

        if (elseCommand.contains(THEN_LITERAL) || elseCommand.contains(ELSE_LITERAL)) {
            TextHelper.sendTextByKey(source, "if.matcher.ambiguity");
            throw new AbortCommandExecutionException();
        }
    }

    @CommandNode("IF")
    @CommandRequirement(level = 4)
    private static int $if(@CommandSource ServerCommandSource source, GreedyString rest) {
        String $rest = rest.getValue();

        Matcher matcher = IF_COMMAND_PARSER.matcher($rest);
        if (matcher.find()) {
            String conditionCommand = matcher.group(1);
            String thenCommand = matcher.group(2);
            String elseCommand = matcher.group(3);
            checkAmbiguity(source, conditionCommand, thenCommand, elseCommand);

            LogUtil.debug("Execute an `/IF` command: condition-command = {}, then-command = {}, else-command = {}", conditionCommand, thenCommand, elseCommand);
            int conditionValue = CommandExecutor.execute(ExtendedCommandSource.fromSource(source), conditionCommand, IfInitializer::handleIfCommandException);
            if (CommandHelper.Return.isSuccess(conditionValue)) {
                CommandExecutor.execute(ExtendedCommandSource.fromSource(source), thenCommand, IfInitializer::handleIfCommandException);
            } else {
                CommandExecutor.execute(ExtendedCommandSource.fromSource(source), elseCommand, IfInitializer::handleIfCommandException);
            }

        } else {
            TextHelper.sendTextByKey(source, "if.matcher.failed");
            return CommandHelper.Return.FAIL;
        }

        return CommandHelper.Return.SUCCESS;
    }

    private static void handleIfCommandException(ExtendedCommandSource context, String commandString, Exception exception) {
        /* Swallow the conditional test failed exception, to prevent feedback-spam. */
        String message = exception.getMessage();
        if (message.startsWith(CONDITIONAL_FAIL_EXCEPTION_PREFIX_STRING)) {
            LogUtil.debug("Swallow the exception with message: {}", message);
            return;
        }

        /* Fallback to original exception handler. */
        CommandExecutor.handleCommandException(context, commandString, exception);
    }

}
