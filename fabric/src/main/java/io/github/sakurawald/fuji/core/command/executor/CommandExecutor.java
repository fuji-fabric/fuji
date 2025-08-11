package io.github.sakurawald.fuji.core.command.executor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CommandExecutor {

    public static void execute(@NotNull ExtendedCommandSource context, @NotNull List<String> commands) {
        commands.forEach(command -> execute(context, command));
    }

    public static int execute(@NotNull ExtendedCommandSource context, @NotNull String command) {
        return execute(context, command, CommandExecutor::handleCommandException);
    }

    @ForDeveloper("""
        Cases:
        1. /run as console bad command
        2. /run as console run as player bad command
        3. /run as console run as player <player> run as console bad command
        4. /run as console run as player %player:name% run as fake-op %player:name% say I am %player:name%
        """)
    public static int execute(@NotNull ExtendedCommandSource context, @NotNull String command, TriConsumer<ExtendedCommandSource, String, Exception> exceptionHandler) {

        /* Expand the command. */
        command = context.expandCommand(command);
        LogUtil.debug("Execute the command: command = `{}`, context = {}", command, context);

        try {
            return Objects
                // NOTE: Use CommandDispatcher to run commands. Since Mojang will do chat message validation for online-mode server, if you are using CommandManager.
                .requireNonNull(CommandHelper.getCommandDispatcher())
                .execute(command, context.getExecutingSource());
        } catch (CommandSyntaxException commandSyntaxException) {
            exceptionHandler.accept(context, command, commandSyntaxException);
            return CommandHelper.Return.FAIL;
        }
    }

    public static void handleCommandException(@NotNull ExtendedCommandSource context, String command, Exception exception) {
        /* Escape tags when reporting an exception. (e.g. "/run as console aa <yellow> bb")*/
        command = TextHelper.Parsers.escapeTags(command);

        // NOTE: Log the console first. (Make the debug easier)
        if (!context.getExecutingSource().isExecutedByPlayer()) {
            LogUtil.warn("Failed to execute command: command = {}, context = {}", command, context);
        }

        /* Echo to the executing source. */
        TextHelper.sendTextByKey(context.getExecutingSource(), "command.execute.echo.executing_source", command, exception.getMessage());

        /* Echo to the initiating source. */
        if (!context.sameSource()) {
            // NOTE: If the executing command source is a dummy server player, then its network handler is null.
            TextHelper.sendTextByKey(context.getInitiatingSource(), "command.execute.echo.initiating_source", command, context.getExecutingSource().getName(), exception.getMessage());
        }
    }
}
