package io.github.sakurawald.fuji.module.initializer.command_meta.$if;

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
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1753515891751L, value = """
    This module provides a simple `/if` command.
    It can be used to express `conditional logic`, such as `if ... then ... else ...`.
    """)
@ColorBox(id = 1753515940860L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    The syntax of `/if` command is: `/if \\<condition-command\\> then \\<then-command\\> else \\<else-command\\>`

    The execution flow of this command is as follows:
    1. Start the `execution` of a given `/if` command instance.
    2. Check the possible `ambiguity` of the given `/if` command instance.
    2.a. If `no ambiguity` found, goto step `3.`
    2.b. If there is `an ambiguity` found, abort the entire execution of `/if` command.
    3. Execute the `condition-command`, and get its `return value`:
    3.a. If `the return value` is `true`, then `executes` the `then-command`.
    3.b. If `the return value` is `false`, then `executes` the `else-command`.

    ◉ Any `failure` will lead to the execution of `else-command`.
    <red>If there is any `error` during the `execution` of `condition-command`, the `else-command` will be executed.
    For example, the `/if bad command then say true else say false` command will do the following things:
    1. First, execute the `/bad command`.
    2. Due to the `error` during the execution of `/bad command`, the `/say false` command will be executed.

    ◉ `Recursive if` is not supported.
    The `/if` command is a `simple` enough command, to provide a very basic `conditional expression`.
    The `nested if` is not supported.
    Once there is an `ambiguity` found in the `/if` command, the entire execution of the `/if` command will be aborted.
    """)
@ColorBox(id = 1753521193815L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Check whether a player is an `operator`.
    Issue: `/if is-op? %player:name% then say It is op. else say It is not op.`

    ◉ Check whether a player has sufficient balance.
    Issue: `/if has-currency? %player:name% fuji:gold 100 then say Sufficient balance. else say Insufficient balance.`

    ◉ Use a shorten version of `/if ... then ...` command.
    The /nop command `performs no action` and always `returns success`.
    You can use it as a `dummy command` in the `else-command` place, to effectively ignore it.
    Issue: `/if is-op? %player:name% then say It is op. else nop`

    ◉ Leverage the power of `/execute if` and `/execute unless` commands.
    Issue: `/if execute if entity @s[x=0,y=64,z=0,distance=..128] then say You are near the origin. else say You are far from the origin.`

    ◉ Combine 2 condition commands, and only execute the `then-command` when they are all `true`.
    """)
public class IfInitializer extends ModuleInitializer {

    private static final Pattern IF_COMMAND_PARSER = Pattern.compile("(.+)\\s+then\\s+(.+)\\s+else\\s+(.+)");
    private static final String THEN_LITERAL = " then";
    private static final String ELSE_LITERAL = " else";

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

    @CommandNode("if")
    @CommandRequirement(level = 4)
    private static int $if(@CommandSource ServerCommandSource source, GreedyString rest) {
        String $rest = rest.getValue();

        Matcher matcher = IF_COMMAND_PARSER.matcher($rest);
        if (matcher.find()) {
            String conditionCommand = matcher.group(1);
            String thenCommand = matcher.group(2);
            String elseCommand = matcher.group(3);
            checkAmbiguity(source, conditionCommand, thenCommand, elseCommand);

            LogUtil.debug("Execute an `/if` command: condition-command = {}, then-command = {}, else-command = {}", conditionCommand, thenCommand, elseCommand);
            int conditionValue = CommandExecutor.execute(ExtendedCommandSource.fromSource(source), conditionCommand);
            if (conditionValue > 0) {
                CommandExecutor.execute(ExtendedCommandSource.fromSource(source), thenCommand);
            } else {
                CommandExecutor.execute(ExtendedCommandSource.fromSource(source), elseCommand);
            }

        } else {
            TextHelper.sendTextByKey(source, "if.matcher.failed");
            return CommandHelper.Return.FAIL;
        }

        return CommandHelper.Return.SUCCESS;
    }

}
