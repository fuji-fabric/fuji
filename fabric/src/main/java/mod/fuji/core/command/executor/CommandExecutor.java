package mod.fuji.core.command.executor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import java.util.List;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.ChatFormatting;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

public class CommandExecutor {

    public static @NotNull List<Integer> executeBatch(@NotNull ExtendedCommandSource context, @NotNull List<String> commands) {
        return commands
            .stream()
            .map(command -> executeSingle(context, command))
            .toList();
    }

    public static int executeSingle(@NotNull ExtendedCommandSource context, @NotNull String command) {
        return executeSingle(context, command, CommandExecutor::handleCommandExecutorException);
    }

    /**
 *         Cases:
        1. /run as console bad command
        2. /run as console run as player bad command
        3. /run as console run as player <player> run as console bad command
        4. /run as console run as player %player:name% run as fake-op %player:name% say I am %player:name%

 **/
    public static int executeSingle(@NotNull ExtendedCommandSource context, @NotNull String command, TriConsumer<ExtendedCommandSource, String, Exception> exceptionHandler) {

        /* Expand the command. */
        command = context.expandCommand(command);
        LogUtil.debug("Command Executor -> Execute the command: command = `{}`, context = {}", command, context);

        try {
            return
                // NOTE: Use CommandDispatcher to run commands. Since Mojang will do chat message validation for online-mode server, if you are using CommandManager.
                CommandHelper
                    .getCommandDispatcher()
                    .execute(command, context.getExecutingSource());
        } catch (CommandSyntaxException commandSyntaxException) {
            exceptionHandler.accept(context, command, commandSyntaxException);
            return CommandHelper.Return.FAILURE;
        }
    }

    public static void handleCommandExecutorException(@NotNull ExtendedCommandSource context, String command, Exception exception) {
        /* Escape tags when reporting an exception. (e.g. "/run as console aa <yellow> bb")*/
        command = TextHelper.Parsers.escapeTags(command);

        /* Log the console if the command is executed by the console. */
        if (!context.getExecutingSource().isPlayer()) {
            LogUtil.warn("Failed to execute command: command = {}, context = {}, exception = {}", command, context, exception);
        }

        /* Echo to the initiating source. */
        // NOTE: If the executing command source is a dummy server player, then its network handler is null.
        TextHelper.sendTextByKey(context.getInitiatingSource(), "command.execute.echo.initiating_source", command, context.getExecutingSource().getTextName(), exception.getMessage());

        // If it's a command syntax exception, stream it to the initialing source.
        if (exception instanceof CommandSyntaxException commandSyntaxException) {
            TextHelper.sendMessageByText(context.getInitiatingSource(), TextHelper.TEXT_EMPTY);
            TextHelper.sendTextByKey(context.getInitiatingSource(), "command.execute.echo.execution.feedback.stream.header");
            sendCommandSyntaxExceptionErrorText(context.getInitiatingSource(), command, commandSyntaxException);
            TextHelper.sendTextByKey(context.getInitiatingSource(), "command.execute.echo.execution.feedback.stream.footer");
        }

        /* Echo to the executing source. */
        if (!context.sameSource()) {
            TextHelper.sendTextByKey(context.getExecutingSource(), "command.execute.echo.executing_source", command, exception.getMessage());
        }
    }

    private static void sendCommandSyntaxExceptionErrorText(@NotNull CommandSourceStack serverCommandSource, @NotNull String commandString, @NotNull CommandSyntaxException commandSyntaxException) {
        serverCommandSource.sendFailure(ComponentUtils.fromMessage(commandSyntaxException.getRawMessage()));
        if (commandSyntaxException.getInput() != null && commandSyntaxException.getCursor() >= 0) {
            int i = Math.min(commandSyntaxException.getInput().length(), commandSyntaxException.getCursor());

            MutableComponent mutableText = Component.empty().withStyle(ChatFormatting.GRAY).withStyle(style -> {
                String suggestionString = "/" + commandString;
                return style
                    .withClickEvent(TextHelper.Events.ClickEvent.makeSuggestCommandAction(suggestionString));
            });

            if (i > 10) {
                mutableText.append(CommonComponents.ELLIPSIS);
            }

            mutableText.append(commandSyntaxException.getInput().substring(Math.max(0, i - 10), i));
            if (i < commandSyntaxException.getInput().length()) {
                MutableComponent text = Component.literal(commandSyntaxException.getInput().substring(i)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
                mutableText.append(text);
            }
            mutableText.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            serverCommandSource.sendFailure(mutableText);
        }
    }
}
