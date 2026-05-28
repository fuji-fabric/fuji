package mod.fuji.module.initializer.command_event;

import java.util.List;
import java.util.Optional;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerDeathEvent;
import mod.fuji.core.event.message.player.PlayerJoinedEvent;
import mod.fuji.core.event.message.player.PlayerLeftEvent;
import mod.fuji.core.event.message.player.PlayerWorldChangedEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStoppingEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_event.config.model.CommandEventConfigModel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Document(id = 1751826634816L, value = """
    This module allows to define `commands` to react to a target `event`.
    When an interesting event occurs, the specified commands will be executed, as the response.
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
@ColorBox(id = 1758083275998L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Teleport the dead player to specified position.
    YOu can use the player death event.
    1. `run as fake-op %player:name% tppos --dimension minecraft:overworld --x 32 --y 64 --z 128`
    """)
public class CommandEventInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandEventConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandEventConfigModel.class);

    public static void executeCommandOnEvent(@Nullable ServerPlayer player, @NotNull List<String> commands) {
        CommandSourceStack commandSource = Optional
            .ofNullable(player)
            .map($player -> player.createCommandSourceStack())
            .orElseGet(CommandHelper.Source::getConsoleCommandSource);

        CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(commandSource), commands);
    }

    @EventConsumer
    private static void consumePlayerJoinedEvent(PlayerJoinedEvent event) {
        ServerPlayer player = event.getPlayer();

        var onPlayerJoinedConfig = CommandEventInitializer.config.model().getEvent().getOnPlayerJoined();
        if (onPlayerJoinedConfig.isEnable()) {
            CommandEventInitializer.executeCommandOnEvent(player, onPlayerJoinedConfig.getCommands());
        }

        var onPlayerFirstJoinedConfig = CommandEventInitializer.config.model().getEvent().getOnPlayerFirstJoined();
        if (onPlayerFirstJoinedConfig.isEnable()) {
            // NOTE: If you use `Stats.LEAVE_GAME < 1` as the stat value, then it will not get saved when the server is stopped by `/stop`.
            // The vanilla Minecraft thinks the `dis-connect` by the server is not identical to `leave the game` by the player.
            int stat = player.getStats().getValue(Stats.CUSTOM.get(Stats.TOTAL_WORLD_TIME));
            if (stat == 0) {
                CommandEventInitializer.executeCommandOnEvent(player, onPlayerFirstJoinedConfig.getCommands());
            }
        }
    }

    @EventConsumer
    private static void consumePlayerLeftEvent(PlayerLeftEvent event) {
        var config = CommandEventInitializer.config.model().getEvent().getOnPlayerLeft();
        if (config.isEnable()) {
            executeCommandOnEvent(event.getPlayer(), config.getCommands());
        }
    }

    @EventConsumer
    private static void consumePlayerDeathEvent(PlayerDeathEvent event) {
        var config = CommandEventInitializer.config.model().getEvent().getOnPlayerDeath();
        if (config.isEnable()) {
            ServerPlayer player = event.getPlayer();
            CommandEventInitializer.executeCommandOnEvent(player, config.getCommands());
        }
    }

    @EventConsumer
    private static void consumePlayerWorldChangedEvent(PlayerWorldChangedEvent event) {
        var config = CommandEventInitializer.config.model().getEvent().getAfterPlayerChangeWorld();
        if (config.isEnable()) {
            CommandEventInitializer.executeCommandOnEvent(event.getPlayer(), config.getCommands());
        }
    }

    @EventConsumer
    private static void consumeOnServerStartedEvent(@Unused ServerStartedEvent event) {
        var config = CommandEventInitializer.config.model().getEvent().getOnServerStarted();
        if (config.isEnable()) {
            CommandEventInitializer.executeCommandOnEvent(null, config.getCommands());
        }
    }

    @EventConsumer
    private static void consumeOnServerStoppingEvent(@Unused ServerStoppingEvent event) {
        var config = CommandEventInitializer.config.model().getEvent().getOnServerStopping();
        if (config.isEnable()) {
            CommandEventInitializer.executeCommandOnEvent(null, config.getCommands());
        }
    }
}
