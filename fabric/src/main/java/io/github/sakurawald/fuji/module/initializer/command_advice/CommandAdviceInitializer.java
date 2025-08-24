package io.github.sakurawald.fuji.module.initializer.command_advice;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
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

    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    public static void processCommandAdvice(@NotNull Object executor, @NotNull ServerCommandSource source, @NotNull String commandString, @NotNull CommandAdviceType adviceType, @NotNull CallbackInfo ci) {
        LogUtil.debug("Process Command Advice: advice type = {}, command string = {}, command source = {}, executor = {}, ", adviceType, commandString, source.getName(), executor);

        /* Create the command advice stream. */
        Stream<CommandAdviceEntry> effectiveCommandAdvices = config.model()
            .getAdvices()
            .stream();

        /* Filter by enable property. */
        effectiveCommandAdvices = effectiveCommandAdvices.filter(CommandAdviceEntry::isEnable);

        /* Filter by advice type. */
        effectiveCommandAdvices = effectiveCommandAdvices.filter(
                it -> it.getAdviceType().equals(adviceType)
                    || (CommandAdviceType.isCancellableAdviceType(it.getAdviceType()) && adviceType.equals(CommandAdviceType.BEFORE_EXECUTING)));

        /* Filter by command source type. */
        effectiveCommandAdvices = effectiveCommandAdvices
            .filter(it -> !it.getMatcher().isExecutedByPlayerOnly() || source.isExecutedByPlayer());

        /* Filter by command string regex. */
        effectiveCommandAdvices = effectiveCommandAdvices
            .filter(it -> commandString.matches(it.getMatcher().getCommandStringRegex()));

        /* Perform advices. */
        effectiveCommandAdvices
            .forEach(commandAdvice -> {
                /* Cancel the executing of target command. */
                if (CommandAdviceType.isCancellableAdviceType(commandAdvice.getAdviceType())) {
                    LogUtil.debug("Cancel the executing of target command {}. (advice = {})", commandString, commandAdvice);

                    if (ci instanceof CallbackInfoReturnable<?>) {
                        ((CallbackInfoReturnable<Integer>) ci).setReturnValue(commandAdvice.getAdviceType().getAlternativeReturnValue());
                    } else {
                        ci.cancel();
                    }
                }

                /* Expand the captured-groups on commands. */
                Matcher matcher = commandAdvice
                    .getMatcher()
                    .getCachedPattern()
                    .matcher(commandString);
                matcher.find();
                List<String> commands = commandAdvice.getCommands()
                    .stream()
                    .map(cmd -> StringUtil.replaceAllAndResetMatcher(matcher, cmd))
                    .collect(Collectors.toCollection(ArrayList::new));

                /* Execute the commands */
                LogUtil.debug("Execute commands {} for {}", commands, commandAdvice);
                CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(source), commands);
            });


    }
}
