package io.github.sakurawald.fuji.module.initializer.command_spy;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.command.BeforeCommandExecutionEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_spy.config.model.CommandSpyConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_spy.config.transformer.CommandSpySchemaV1Transformer;
import io.github.sakurawald.fuji.module.initializer.command_spy.structure.CommandSpyRule;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826800901L, value = """
    This module allows you to spy `command execution`.
    """)
@ColorBox(id = 1757360673310L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    1. Match the defined `rules` in top-down order.
    2. Find the `first matched rule`.
    3. Apply the `first matched rule`, if it exists.
    """)
public class CommandSpyInitializer extends ModuleInitializer {
    private static final BaseConfigurationHandler<CommandSpyConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandSpyConfigModel.class)
        .installTransformer(new CommandSpySchemaV1Transformer());

    private static void processCommandSpy(@NotNull ServerCommandSource commandSource, @NotNull String commandString) {
        LogUtil.debug("Process Command Spy: command source = {}, command string = {}", commandSource.getName(), commandString);

        /* Find first matched rule. */
        Optional<CommandSpyRule> matchedRule = config.model()
            .getRules()
            .stream()
            .filter(CommandSpyRule::isEnable)
            .filter(rule -> {
                CommandSpyRule.Matcher matcher = rule.getMatcher();
                if (!matcher.isAcceptSilentCommand() && CommandHelper.Source.isSilent(commandSource)) return false;
                if (!matcher.isAcceptPlayerCommandSource() && CommandHelper.Source.isExecutedByPlayer(commandSource)) return false;
                if (!matcher.isAcceptServerCommandSource() && CommandHelper.Source.isExecutedByConsole(commandSource)) return false;

                return matcher.getCachedPattern()
                    .matcher(commandString)
                    .matches();
            })
            .findFirst();

        /* Apply the matched rule. */
        matchedRule
            .ifPresent(rule -> {
                CommandSpyRule.IfMatched ifMatched = rule.getIfMatched();

                /* Perform action set. */
                logToConsole(commandSource, commandString, ifMatched);
                notifyPlayersWithLevelPermission(commandSource, commandString, ifMatched.getNotifyPlayersWithLevelPermission());
            });
    }

    private static void logToConsole(@NotNull ServerCommandSource commandSource, @NotNull String commandString, @NotNull CommandSpyRule.IfMatched ifMatched) {
        if (ifMatched.isLogToConsole()) {
            LogUtil.info("{} issued the server command: /{}", commandSource.getName(), commandString);
        }
    }

    private static void notifyPlayersWithLevelPermission(@NotNull ServerCommandSource commandSource, @NotNull String commandString, int notifyPlayersWithLevelPermission) {
        PlayerHelper.Lookup.getOnlinePlayers()
            .stream()
            .filter(player -> player.hasPermissionLevel(notifyPlayersWithLevelPermission))
            .forEach(player -> {
                TextHelper.sendTextByKey(player, "command_spy.notify", commandSource.getName(), commandString);
            });
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority = EventConsumer.LOWEST)
    private static void consumeBeforeCommandExecutionEvent(BeforeCommandExecutionEvent event) {
        CommandSpyInitializer.processCommandSpy(
            event.getCommandSource(),
            event.getCommandString()
        );
    }
}
