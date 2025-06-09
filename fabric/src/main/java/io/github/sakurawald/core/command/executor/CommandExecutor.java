package io.github.sakurawald.core.command.executor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;


@UtilityClass
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

        try {
            return Objects
                .requireNonNull(ServerHelper.getCommandDispatcher())
                .execute(command, context.getExecutingSource());
        } catch (CommandSyntaxException e) {
            // echo to the executing source
            TextHelper.sendMessageByKey(context.getExecutingSource(), "command.execute.echo.executing_source", command, e.getMessage());

            // echo to the initiating source
            if (!context.sameSource()) {
                TextHelper.sendMessageByKey(context.getInitiatingSource(), "command.execute.echo.initiating_source", command, context.getExecutingSource().getName(), e.getMessage());
            }
        }

        return CommandHelper.Return.FAIL;
    }
}
