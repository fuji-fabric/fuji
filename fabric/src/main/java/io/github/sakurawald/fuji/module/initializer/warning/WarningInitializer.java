package io.github.sakurawald.fuji.module.initializer.warning;


import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.warning.config.model.WarningConfigModel;
import io.github.sakurawald.fuji.module.initializer.warning.config.model.WarningDataModel;
import io.github.sakurawald.fuji.module.initializer.warning.gui.WarningGui;
import io.github.sakurawald.fuji.module.initializer.warning.service.WarningService;
import io.github.sakurawald.fuji.module.initializer.warning.structure.Warning;
import io.github.sakurawald.fuji.module.initializer.warning.structure.WarningRule;
import io.github.sakurawald.fuji.module.initializer.warning.structure.PlayerWarnings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document("""
    This module provides the `information management` for `staffs`.
    A shared `warning book` to `track` the behaviours of players, between `staffs`.
    You can `create` a `warning` for a `player`.
    All `staffs` can `view` the `warnings` of a `player`.

    You can use `warnings` to `track` the behaviours of a `player`.
    """)
public class WarningInitializer extends ModuleInitializer {


    public static PermissionDescriptor CREATE_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.create", """
        To `create` a new `warning` for a `player`.
        """);

    public static PermissionDescriptor READ_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.read", """
        To `read` the `warnings` of a `player`.
        """);

    public static PermissionDescriptor UPDATE_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.update", """
        To `update` the `warnings` of a `player`.
        """);

    public static PermissionDescriptor DELETE_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.delete", """
        To `delete` an existed `warning` of a `player`.
        """);

    public static PermissionDescriptor NOTIFY_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.notify", """
        When a `player` with `warnings` join/leave the server, you will get notified.
        """);

    public static final BaseConfigurationHandler<WarningConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WarningConfigModel.class);

    public static final BaseConfigurationHandler<WarningDataModel> data = new ObjectConfigurationHandler<>("warning-data.json", WarningDataModel.class);

    @Document("Open the warning GUI.")
    @CommandNode("warning")
    @CommandRequirement(level = 4)
    private static int $warningRoot(@CommandSource ServerPlayerEntity player) {
        $warningGui(player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Open the warning GUI.")
    @CommandNode("warning gui")
    @CommandRequirement(level = 4)
    private static int $warningGui(@CommandSource ServerPlayerEntity player) {
        List<String> offlinePlayerNames = ServerHelper.getOfflinePlayerNames();
        new WarningGui(null, player, offlinePlayerNames, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Create a new warning for the player.")
    @CommandNode("warning create")
    @CommandRequirement(level = 4)
    private static int $createWarning(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer, GreedyString warning) {
        String creatorName = source.getName();
        String targetPlayerName = targetPlayer.getValue();
        String warningDescription = warning.getValue();

        WarningService.createWarning(creatorName, targetPlayerName, warningDescription);
        TextHelper.sendMessageByKey(source, "warning.created", targetPlayerName);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("List the warnings of a player.")
    @CommandNode("warning list")
    @CommandRequirement(level = 4)
    private static int $listWarning(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer) {
        String targetPlayerName = targetPlayer.getValue();
        PlayerWarnings playerWarnings = WarningInitializer.getPlayerWarnings(targetPlayerName);
        TextHelper.sendMessageByKey(source, "warning.list.message", targetPlayerName, playerWarnings.warnings.size());

        playerWarnings.warnings.forEach(warning -> {
            warning
                .asLore(source)
                .forEach(source::sendMessage);

            source.sendMessage(TextHelper.TEXT_EMPTY);
        });

        return CommandHelper.Return.SUCCESS;
    }

    @Document("Clear the warnings of a player.")
    @CommandNode("warning clear")
    @CommandRequirement(level = 4)
    private static int $clearWarning(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer) {
        String targetPlayerName = targetPlayer.getValue();
        List<Warning> warnings = WarningInitializer.getPlayerWarnings(targetPlayerName).warnings;
        int originalSize = warnings.size();
        warnings.clear();
        WarningInitializer.data.writeStorage();

        TextHelper.sendMessageByKey(source, "warning.clear", originalSize, targetPlayerName);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Clear all warnings for all players.")
    @CommandNode("warning clear-all")
    @CommandRequirement(level = 4)
    private static int $clearAllWarnings(@CommandSource ServerCommandSource source, Optional<Boolean> confirm) {
        Boolean confirmed = confirm.orElse(false);
        if (!confirmed) {
            TextHelper.sendMessageByKey(source, "operation.cancelled");
            return CommandHelper.Return.SUCCESS;
        }

        WarningInitializer.data.model().players = new ArrayList<>();
        WarningInitializer.data.writeStorage();

        TextHelper.sendMessageByKey(source, "warning.clear_all");
        return CommandHelper.Return.SUCCESS;
    }

    public static PlayerWarnings getPlayerWarnings(String playerName) {
        /* Return existed player warnings. */
        List<PlayerWarnings> players = data.model().players;
        Optional<PlayerWarnings> playerWarningsOpt = players
            .stream()
            .filter(it -> it.player.equals(playerName))
            .findFirst();
        if (playerWarningsOpt.isPresent()) {
            return playerWarningsOpt.get();
        }

        /* Make a new one. */
        PlayerWarnings playerWarnings = new PlayerWarnings(playerName);
        players.add(playerWarnings);
        data.writeStorage();
        return playerWarnings;
    }

    @Override
    protected void onInitialize() {
        PlayerEvents.ON_PLAYER_JOINED.register(player -> processNotify(player, true));
        PlayerEvents.ON_PLAYER_LEAVE.register(player -> processNotify(player, false));
    }

    public static void processNotify(ServerPlayerEntity targetPlayer, boolean isJoin) {
        /* Does the player have any warnings? */
        String playerName = PlayerHelper.getPlayerName(targetPlayer);
        PlayerWarnings playerWarnings = getPlayerWarnings(playerName);
        if (playerWarnings.warnings.isEmpty()) return;

        /* Send notify to online staffs. */
        ServerHelper
            .getOnlinePlayers()
            .stream()
            .filter(it -> LuckpermsHelper.hasPermission(it.getUuid(), NOTIFY_WARNINGS_PERMISSION))
            .forEach(it -> {
                int warningsSize = playerWarnings.warnings.size();
                if (isJoin) {
                    TextHelper.sendMessageByKey(it, "warning.notify.join", playerName, warningsSize);
                } else {
                    TextHelper.sendMessageByKey(it, "warning.notify.leave", playerName, warningsSize);
                }
            });
    }


    public static void processWarningRules(String targetPlayerName) {
        Optional<WarningRule> first = config.model().rules
            .stream()
            // Sort the higher value first.
            .sorted(Comparator
                .comparing(WarningRule::getIfNumberOfWarningsGreaterEqualThan)
                .reversed())
            .filter(it -> {
                int numberOfWarnings = WarningInitializer.getPlayerWarnings(targetPlayerName)
                    .warnings.size();
                return numberOfWarnings >= it.getIfNumberOfWarningsGreaterEqualThan();
            })
            .findFirst();

        if (first.isPresent()) {
            WarningRule warningRule = first.get();

            /* Execute the warning rule. */
            LogUtil.info("Execute the warning rule for player {}: warning rule = {}", targetPlayerName, warningRule);

            // NOTE: Load the dummy offline server player entity, to provide the placeholder parsing context. (Use `/when-online` to execute commands on real server player entity.)
            ServerCommandSource offlineServerCommandSource = PlayerHelper
                .loadOfflinePlayer(targetPlayerName)
                .getCommandSource();
            ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(offlineServerCommandSource);

            List<String> commands = warningRule.commands;
            CommandExecutor.execute(extendedCommandSource, commands);
        }

    }
}
