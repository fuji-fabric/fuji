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
import java.util.Optional;
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

    public static void processCommandAdvice(@NotNull Object executor, @NotNull ServerCommandSource source, @NotNull String commandString, @NotNull CommandAdviceType adviceType, @NotNull Optional<CallbackInfo> callbackInfo, @NotNull Optional<Integer> targetCommandReturnValue) {
        LogUtil.debug("Process Command Advice: advice type = {}, command string = {}, command source = {}, executor = {}, target command return value = {}", adviceType, commandString, source.getName(), executor, targetCommandReturnValue);

        /* Create the command advice stream. */
        Stream<CommandAdviceEntry> filterCommandAdvices = config.model()
            .getAdvices()
            .stream();

        /* Filter by enable property. */
        filterCommandAdvices = filterCommandAdvices.filter(CommandAdviceEntry::isEnable);

        /* Filter by advice type. */
        filterCommandAdvices = filterCommandAdvices.filter(
            it -> it.getAdviceType().equals(adviceType)
                // NOTE: All canceller-advice must be performed before the target command execution.
                || (it.getAdviceType().isCanceller() && adviceType.equals(CommandAdviceType.BEFORE_EXECUTION))
                || (it.getAdviceType().equals(CommandAdviceType.ON_EXECUTION_CANCELLED) && adviceType.equals(CommandAdviceType.BEFORE_EXECUTION))
        );

        /* Filter by command source type. */
        filterCommandAdvices = filterCommandAdvices
            .filter(it -> !it.getMatcher().isExecutedByPlayerOnly() || source.isExecutedByPlayer());

        /* Filter by command string regex. */
        filterCommandAdvices = filterCommandAdvices
            .filter(it -> commandString.matches(it.getMatcher().getCommandStringRegex()));

        /* Collect effective command advices. */
        List<CommandAdviceEntry> effectiveCommandAdvices = filterCommandAdvices.toList();

        /* Perform canceller advices.  */
        AtomicBoolean targetCommandExecutionCancelled = new AtomicBoolean(false);
        effectiveCommandAdvices
            .stream()
            .filter(it -> it.getAdviceType().isCanceller())
            .forEach(it -> {
                /* Short-circuit: If the target command execution has already been cancelled by other canceller-advice. */
                if (targetCommandExecutionCancelled.get()) {
                    return;
                }

                /* Execute the advices commands to get the return values. */
                @NotNull List<Integer> adviceCommandReturnValues = executeAdviceCommands(source, commandString, it);

                /* Cancel the target command execution conditionally/un-conditionally. */
                if (it.getAdviceType().equals(CommandAdviceType.CANCEL_IF_ANY_SUCCESS)) {
                    if (adviceCommandReturnValues.stream().anyMatch(CommandHelper.Return::isSuccess)) {
                        cancelTargetCommandExecution(commandString, it, targetCommandExecutionCancelled, callbackInfo);
                    }
                } else if (it.getAdviceType().equals(CommandAdviceType.CANCEL_IF_ALL_SUCCESS)) {
                    if (adviceCommandReturnValues.stream().allMatch(CommandHelper.Return::isSuccess)) {
                        cancelTargetCommandExecution(commandString, it, targetCommandExecutionCancelled, callbackInfo);
                    }
                } else {
                    // Un-conditionally canceller-advice types: CANCEL_WITH_SUCCESS, CANCEL_WITH_FAILURE
                    cancelTargetCommandExecution(commandString, it, targetCommandExecutionCancelled, callbackInfo);
                }

            });

        /* If the target command execution is cancelled, perform the cleanup things, and exit. */
        if (targetCommandExecutionCancelled.get()) {
            /* Perform EXECUTION_CANCELLED advices. */
            effectiveCommandAdvices
                .stream()
                .filter(it -> it.getAdviceType().equals(CommandAdviceType.ON_EXECUTION_CANCELLED))
                .forEach(it -> executeAdviceCommands(source, commandString, it));

            /* Exit the process. */
            return;
        }

        /* Perform non-canceller advices. */
        effectiveCommandAdvices
            .stream()
            .filter(it -> !it.getAdviceType().isCanceller()
                && !it.getAdviceType().equals(CommandAdviceType.ON_EXECUTION_CANCELLED)
            )
            .forEach(it -> {
                if (it.getAdviceType().equals(CommandAdviceType.BEFORE_EXECUTION) || it.getAdviceType().equals(CommandAdviceType.AFTER_EXECUTION)) {
                    executeAdviceCommands(source, commandString, it);
                } else if (it.getAdviceType().equals(CommandAdviceType.ON_EXECUTION_SUCCESS) || it.getAdviceType().equals(CommandAdviceType.ON_EXECUTION_FAILURE)) {
                    targetCommandReturnValue.ifPresentOrElse($targetCommandReturnValue -> {
                        boolean success = CommandHelper.Return.isSuccess($targetCommandReturnValue);

                        if (it.getAdviceType().equals(CommandAdviceType.ON_EXECUTION_SUCCESS) && success) {
                            executeAdviceCommands(source, commandString, it);
                            return;
                        }

                        if (it.getAdviceType().equals(CommandAdviceType.ON_EXECUTION_FAILURE) && !success) {
                            executeAdviceCommands(source, commandString, it);
                            return;
                        }

                    }, () -> LogUtil.debug("The return value of target command {} is null, can't perform the command advice {}.", commandString, it));
                }
            });

    }

    @SuppressWarnings({"unchecked"})
    private static void cancelTargetCommandExecution(@NotNull String commandString, @NotNull CommandAdviceEntry commandAdvice, @NotNull AtomicBoolean targetCommandExecutionCancelled, @NotNull Optional<CallbackInfo> callbackInfo) {
        /* Mark to exit the advice processing. */
        targetCommandExecutionCancelled.set(true);

        /* Cancel the target command execution. */
        callbackInfo
            .ifPresentOrElse($callbackInfo -> {
                LogUtil.debug("Cancel the executing of target command {}. (advice = {})", commandString, commandAdvice);

                if ($callbackInfo instanceof CallbackInfoReturnable<?>) {
                    ((CallbackInfoReturnable<Integer>) $callbackInfo).setReturnValue(commandAdvice.getAdviceType().getAlternativeReturnValue());
                } else {
                    $callbackInfo.cancel();
                }

            }, () -> LogUtil.warn("Failed to cancel the execution of target command {}, due to the Optional<CallbackInfo> is empty. (advice = {})", commandString, commandAdvice));

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
