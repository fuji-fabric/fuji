package io.github.sakurawald.fuji.core.command.executor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CommandExecutor {

    public static void execute(@NotNull ExtendedCommandSource context, @NotNull List<String> commands) {
        commands.forEach(command -> execute(context, command));
    }

    /**
     * cases:
     * - /run as console bad command
     * - /run as console run as player bad command
     * - /run as console run as player <player> run as console bad command
     */
    public static int execute(@NotNull ExtendedCommandSource context, @NotNull String command) {
        /* expand the command */
        command = context.expandCommand(command);
        LogUtil.debug("Executing command: command = `{}`, context = {}", command, context);

        try {
            return Objects
                // NOTE: Use CommandDispatcher to run commands. Since Mojang will do chat message validation for online-mode server, if you are using CommandManager.
                .requireNonNull(ServerHelper.getCommandDispatcher())
                .execute(command, context.getExecutingSource());
        } catch (CommandSyntaxException e) {
            /* Escape tags when reporting an exception. (e.g. "/run as console aa <yellow> bb")*/
            command = TextHelper.escapeTags(command);

            /* Echo to the executing source. */
            TextHelper.sendMessageByKey(context.getExecutingSource(), "command.execute.echo.executing_source", command, e.getMessage());

            /* Echo to the initiating source. */
            if (!context.sameSource()) {
                TextHelper.sendMessageByKey(context.getInitiatingSource(), "command.execute.echo.initiating_source", command, context.getExecutingSource().getName(), e.getMessage());
            }

            /* Echo to the console, if the command is executed by console. */
            if (!context.getExecutingSource().isExecutedByPlayer()) {
                LogUtil.warn("Failed to execute command: command = {}, context = {}", command, context);
            }
        }

        return CommandHelper.Return.FAIL;
    }
}
