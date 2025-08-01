package io.github.sakurawald.fuji.module.initializer.warning.service;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.warning.WarningInitializer;
import io.github.sakurawald.fuji.module.initializer.warning.structure.PlayerWarnings;
import io.github.sakurawald.fuji.module.initializer.warning.structure.Warning;
import io.github.sakurawald.fuji.module.initializer.warning.structure.WarningRule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class WarningService {

    public static void createWarning(String creatorName, String targetPlayerName, String warningDescription) {
        /* Create a new warning for the target player. */
        Warning newWarning = Warning.makeWarning(creatorName, warningDescription);
        getPlayerWarnings(targetPlayerName)
            .warnings
            .add(newWarning);
        WarningInitializer.data.writeStorage();

        /* Process the warning rules. */
        processWarningRules(targetPlayerName);
    }

    public static void deleteWarning(String targetPlayerName, Warning warning) {
        getPlayerWarnings(targetPlayerName)
            .warnings
            .remove(warning);
        WarningInitializer.data.writeStorage();
    }

    public static int clearWarnings(String targetPlayerName) {
        List<Warning> warnings = getPlayerWarnings(targetPlayerName).warnings;
        int originalSize = warnings.size();
        warnings.clear();
        WarningInitializer.data.writeStorage();
        return originalSize;
    }

    public static void clearAllWarnings() {
        WarningInitializer.data.model().players = new ArrayList<>();
        WarningInitializer.data.writeStorage();
    }

    public static PlayerWarnings getPlayerWarnings(String playerName) {
        /* Return existed player warnings. */
        List<PlayerWarnings> players = WarningInitializer.data.model().players;
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
        WarningInitializer.data.writeStorage();
        return playerWarnings;
    }

    public static void processNotify(ServerPlayerEntity targetPlayer, boolean isJoin) {
        /* Does the player have any warnings? */
        String playerName = PlayerHelper.getPlayerName(targetPlayer);
        PlayerWarnings playerWarnings = getPlayerWarnings(playerName);
        if (playerWarnings.warnings.isEmpty()) return;

        /* Send notify to online staffs. */
        PlayerHelper.Lookup
            .getOnlinePlayers()
            .stream()
            .filter(it -> LuckpermsHelper.hasPermission(it.getUuid(), WarningInitializer.NOTIFY_WARNINGS_PERMISSION))
            .forEach(it -> {
                int warningsSize = playerWarnings.warnings.size();
                if (isJoin) {
                    TextHelper.sendTextByKey(it, "warning.notify.join", playerName, warningsSize);
                } else {
                    TextHelper.sendTextByKey(it, "warning.notify.leave", playerName, warningsSize);
                }
            });
    }

    public static void processWarningRules(String targetPlayerName) {
        Optional<WarningRule> first = WarningInitializer.config.model().rules
            .stream()
            // Sort the higher value first.
            .sorted(Comparator
                .comparing(WarningRule::getIfNumberOfWarningsGreaterEqualThan)
                .reversed())
            .filter(it -> {
                int numberOfWarnings = getPlayerWarnings(targetPlayerName)
                    .warnings.size();
                return numberOfWarnings >= it.getIfNumberOfWarningsGreaterEqualThan();
            })
            .findFirst();

        if (first.isPresent()) {
            WarningRule warningRule = first.get();

            /* Execute the warning rule. */
            LogUtil.info("Execute the warning rule for player {}: warning rule = {}", targetPlayerName, warningRule);

            // NOTE: Load the dummy offline server player entity, to provide the placeholder parsing context. (Use `/when-online` to execute commands on real server player entity.)
            ServerCommandSource offlineServerCommandSource = PlayerHelper.Maker
                .loadDummyPlayer(targetPlayerName)
                .getCommandSource();
            ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(offlineServerCommandSource);

            List<String> commands = warningRule.commands;
            CommandExecutor.execute(extendedCommandSource, commands);
        }

    }
}
