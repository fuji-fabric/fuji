package io.github.sakurawald.fuji.module.initializer.command_event;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_event.config.model.CommandEventConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import net.minecraft.stat.Stats;

@Document("""
    This module allows you to execute commands on specified events.
    """)
public class CommandEventInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandEventConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandEventConfigModel.class);

    @Override
    protected void onInitialize() {
        PlayerEvents.ON_PLAYER_JOINED.register(CommandEventInitializer::processOnPlayerJoinedEvent);
        PlayerEvents.ON_PLAYER_LEAVE.register(CommandEventInitializer::processOnPlayerLeaveEvent);
    }

    public static void executeCommandOnEvent(ServerPlayerEntity player, List<String> commands) {
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), commands);
    }

    private static void processOnPlayerJoinedEvent(ServerPlayerEntity player) {
        var onPlayerJoinedConfig = CommandEventInitializer.config.model().event.on_player_joined;
        if (onPlayerJoinedConfig.enable) {
            CommandEventInitializer.executeCommandOnEvent(player, onPlayerJoinedConfig.command_list);
        }

        var onPlayerFirstJoinedConfig = CommandEventInitializer.config.model().event.on_player_first_joined;
        if (onPlayerFirstJoinedConfig.enable) {
            if (player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.LEAVE_GAME)) < 1) {
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

}
