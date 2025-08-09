package io.github.sakurawald.fuji.module.initializer.command_advice;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_advice.model.CommandAdviceConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceEntry;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceType;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private static final BaseConfigurationHandler<CommandAdviceConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandAdviceConfigModel.class);

    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    public static void processCommandAdvice(Object handler, ServerCommandSource source, String commandString, CommandAdviceType adviceType, CallbackInfo ci) {
        // NOTE: If the command is executed by a player, then the CommandManager will call CommandDispatcher.
        // NOTE: If the command is executed by the console, then it will directly call the function in CommandDispatcher.

        // Log it.
        LogUtil.debug("Process Command Advice: advice type = {}, command string = {}, command source = {}, handler = {}, ", adviceType, commandString, source.getName(), handler);

        // Filter the advice entries by advice type.
        List<CommandAdviceEntry> targetAdviceEntries = config.model()
            .entries
            .stream()
            .filter(it -> it.adviceType.equals(adviceType)
            || (it.adviceType.equals(CommandAdviceType.CANCEL_WITH_SUCCESS)
                && adviceType.equals(CommandAdviceType.BEFORE_EXECUTING)))
            .collect(Collectors.toCollection(ArrayList::new));

        // Filter the advice entries by command source type.
        targetAdviceEntries = targetAdviceEntries
            .stream()
            .filter(it -> !it.onlyValidWhenCommandIsExecutedByPlayer || source.isExecutedByPlayer())
            .collect(Collectors.toCollection(ArrayList::new));


        // Perform advice.
        targetAdviceEntries
            .stream()
            .filter(it -> commandString.matches(it.matchCommandStringRegex))
            .forEach(it -> {
                // Cancel the executing of target command.
                if (it.adviceType.equals(CommandAdviceType.CANCEL_WITH_SUCCESS)) {
                    LogUtil.debug("Cancel the executing of target command {} with success for {}", commandString, it);
                    if (ci instanceof CallbackInfoReturnable<?>) {
                        ((CallbackInfoReturnable<Integer>) ci).setReturnValue(CommandHelper.Return.SUCCESS);
                    } else {
                        ci.cancel();
                    }
                }

                // Replace captured-groups for commands.
                Matcher matcher = Pattern
                    .compile(it.matchCommandStringRegex)
                    .matcher(commandString);
                matcher.find();
                List<String> commands = it.commands
                    .stream()
                    .map(cmd -> StringUtil.replaceAllAndResetMatcher(matcher, cmd))
                    .collect(Collectors.toCollection(ArrayList::new));

                // Execute commands
                LogUtil.debug("Execute commands {} for {}", commands, it);
                CommandExecutor.execute(ExtendedCommandSource.asConsole(source), commands);
            });


    }
}
