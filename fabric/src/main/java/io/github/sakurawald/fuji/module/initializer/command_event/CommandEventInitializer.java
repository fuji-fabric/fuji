package io.github.sakurawald.fuji.module.initializer.command_event;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.impl.PlayerEvents;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.OnPlayerDeathEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_event.config.model.CommandEventConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import net.minecraft.stat.Stats;

@Document(id = 1751826634816L, value = """
    This module allows you to execute commands on specified events.
    """)
@ColorBox(id = 1751904334639L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    When an interested event occurs, this module will execute specified commands for that event as console.
    The `contextual player` in that `event` will be used as the `placeholder context`, to parse the `placeholders` in the `command string`.
    """)
@ColorBox(id = 1751904417278L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Send messages on player join.
    You can use `/send-custom` or `/send-message` to send messages when a player joins.

    ◉ Execute commands to welcome a new player.
    You can use the player first join event.
    For example:
    1. `send-broadcast \\<light_purple\\>Welcome new player %player:name% to join us!`
    2. `kit give %player:name% \\<kit-name\\>`
    3. `send-custom as-message %player:name% new-player-guide`
    4. `run as fake-op %player:name% rtp`
    5. `delay 10 spawnpoint %player:name%"`
    """)
public class CommandEventInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandEventConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandEventConfigModel.class);

    @Override
    protected void onInitialize() {
        PlayerEvents.ON_PLAYER_JOINED.register(CommandEventInitializer::processOnPlayerJoinedEvent);
        PlayerEvents.ON_PLAYER_LEAVE.register(CommandEventInitializer::processOnPlayerLeaveEvent);
    }

    public static void executeCommandOnEvent(ServerPlayerEntity player, List<String> commands) {
        CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(player.getCommandSource()), commands);
    }

    private static void processOnPlayerJoinedEvent(ServerPlayerEntity player) {
        var onPlayerJoinedConfig = CommandEventInitializer.config.model().event.on_player_joined;
        if (onPlayerJoinedConfig.enable) {
            CommandEventInitializer.executeCommandOnEvent(player, onPlayerJoinedConfig.command_list);
        }

        var onPlayerFirstJoinedConfig = CommandEventInitializer.config.model().event.on_player_first_joined;
        if (onPlayerFirstJoinedConfig.enable) {
            // NOTE: If you use `Stats.LEAVE_GAME < 1` as the stat value, then it will not get saved when the server is stopped by `/stop`.
            // The vanilla Minecraft thinks the `dis-connect` by the server is not identical to `leave the game` by the player.
            int stat = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.TOTAL_WORLD_TIME));
            if (stat == 0) {
                CommandEventInitializer.executeCommandOnEvent(player, onPlayerFirstJoinedConfig.command_list);
            }
        }
    }

    private static void processOnPlayerLeaveEvent(ServerPlayerEntity player) {
        var config = CommandEventInitializer.config.model().event.on_player_left;
        if (config.enable) {
            executeCommandOnEvent(player, config.command_list);
        }
    }

    @EventConsumer
    private static void handleOnPlayerDeathEvent(OnPlayerDeathEvent event) {
        var config = CommandEventInitializer.config.model().event.on_player_death;
        if (config.enable) {
            ServerPlayerEntity player = event.getPlayer();
            CommandEventInitializer.executeCommandOnEvent(player, config.command_list);
        }
    }

}
