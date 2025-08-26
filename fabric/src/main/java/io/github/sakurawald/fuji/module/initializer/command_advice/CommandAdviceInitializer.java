package io.github.sakurawald.fuji.module.initializer.command_advice;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_advice.config.model.CommandAdviceConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_advice.config.transformer.CommandAdviceV1SchemaTransformer;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceEntry;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Document(id = 1751826306321L, value = """
    This module allows you to decorate existing commands:
    1. Run other commands `before` execution the target command.
    2. Run other commands `after` execution the target command.
    3. Cancel the execution of the target command, and run other commands.
    """)
@ColorBox(id = 1751900137390L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    Execute other commands before/after a target command.
    """)
@ColorBox(id = 1751900375812L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    Decorate an existing command with other commands.
    """)
@ColorBox(id = 1751900379675L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    Cancel the execution of the target command, and execute other commands instead.
    """)
@ColorBox(id = 1751900258020L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    This module provides `similar` functions to `command_bundle` module.
    """)
@TestCase(action = "Issue `/say hi` command.", targets = "The command should be cancelled with the `/send-broadcast` command.")
public class CommandAdviceInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<CommandAdviceConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandAdviceConfigModel.class)
        .installTransformer(new CommandAdviceV1SchemaTransformer());

    public static void processCommandAdvice(@NotNull Object executor, @NotNull ServerCommandSource source, @NotNull String commandString, @NotNull CommandAdviceType adviceType, @NotNull CallbackInfo ci) {
        LogUtil.debug("Process Command Advice: advice type = {}, command string = {}, command source = {}, executor = {}, ", adviceType, commandString, source.getName(), executor);

        /* Create the command advice stream. */
        Stream<CommandAdviceEntry> filterCommandAdvices = config.model()
            .getAdvices()
            .stream();

        /* Filter by enable property. */
        filterCommandAdvices = filterCommandAdvices.filter(CommandAdviceEntry::isEnable);

        /* Filter by advice type. */
        filterCommandAdvices = filterCommandAdvices.filter(
            it -> it.getAdviceType().equals(adviceType)
                // NOTE: All cancellable-advice must be performed before the target command execution.
                || (it.isCancellableAdviceType() && adviceType.equals(CommandAdviceType.BEFORE_EXECUTING)));

        /* Filter by command source type. */
        filterCommandAdvices = filterCommandAdvices
            .filter(it -> !it.getMatcher().isExecutedByPlayerOnly() || source.isExecutedByPlayer());

        /* Filter by command string regex. */
        filterCommandAdvices = filterCommandAdvices
            .filter(it -> commandString.matches(it.getMatcher().getCommandStringRegex()));

        /* Collect effective command advices. */
        List<CommandAdviceEntry> effectiveCommandAdvices = filterCommandAdvices.toList();

        /* Perform cancellable advices.  */
        AtomicBoolean targetCommandExecutionCancelled = new AtomicBoolean(false);
        effectiveCommandAdvices
            .stream()
            .filter(CommandAdviceEntry::isCancellableAdviceType)
            .forEach(commandAdvice -> {
                /* Skip if the target command execution has already been cancelled. */
                if (targetCommandExecutionCancelled.get()) {
                    return;
                }

                /* Execute the advices commands to get the return values. */
                @NotNull List<Integer> adviceCommandReturnValues = executeAdviceCommands(source, commandString, commandAdvice);

                /* Cancel the target command execution conditionally/un-conditionally. */
                if (commandAdvice.getAdviceType().equals(CommandAdviceType.CANCEL_IF_ANY_SUCCESS)) {
                    if (adviceCommandReturnValues.stream().anyMatch(CommandHelper.Return::isSuccess)) {
                        cancelTargetCommandExecution(commandString, commandAdvice, targetCommandExecutionCancelled, ci);
                    }
                } else if (commandAdvice.getAdviceType().equals(CommandAdviceType.CANCEL_IF_ALL_SUCCESS)) {
                    if (adviceCommandReturnValues.stream().allMatch(CommandHelper.Return::isSuccess)) {
                        cancelTargetCommandExecution(commandString, commandAdvice, targetCommandExecutionCancelled, ci);
                    }
                } else {
                    cancelTargetCommandExecution(commandString, commandAdvice, targetCommandExecutionCancelled, ci);
                }

            });

        if (targetCommandExecutionCancelled.get()) {
            return;
        }

        /* Perform non-cancellable advices. */
        effectiveCommandAdvices
            .stream()
            .filter(it -> !it.isCancellableAdviceType())
            .forEach(commandAdvice -> {
                executeAdviceCommands(source, commandString, commandAdvice);
            });
    }

    @SuppressWarnings({"unchecked"})
    private static void cancelTargetCommandExecution(@NotNull String commandString, @NotNull CommandAdviceEntry commandAdvice, @NotNull AtomicBoolean targetCommandExecutionCancelled, @NotNull CallbackInfo ci) {
        LogUtil.debug("Cancel the executing of target command {}. (advice = {})", commandString, commandAdvice);

        targetCommandExecutionCancelled.set(true);

        if (ci instanceof CallbackInfoReturnable<?>) {
            ((CallbackInfoReturnable<Integer>) ci).setReturnValue(commandAdvice.getAdviceType().getAlternativeReturnValue());
        } else {
            ci.cancel();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static @NotNull List<Integer> executeAdviceCommands(@NotNull ServerCommandSource source, @NotNull String commandString, @NotNull CommandAdviceEntry commandAdvice) {
        /* Initialize the matcher .*/
        Matcher matcher = commandAdvice
            .getMatcher()
            .getCachedPattern()
            .matcher(commandString);
        matcher.find();

        /* Expand the captured-groups on commands. */
        List<String> commands = commandAdvice.getCommands()
            .stream()
            .map(cmd -> StringUtil.replaceAllAndResetMatcher(matcher, cmd))
            .collect(Collectors.toCollection(ArrayList::new));

        /* Execute the commands */
        LogUtil.debug("Execute advices commands {}. (advice = {})", commands, commandAdvice);

        List<Integer> adviceCommandReturnValues = CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(source), commands);
        LogUtil.debug("Get advice command return values {}. (advice = {})", adviceCommandReturnValues, commandAdvice);
        return adviceCommandReturnValues;
    }
}
