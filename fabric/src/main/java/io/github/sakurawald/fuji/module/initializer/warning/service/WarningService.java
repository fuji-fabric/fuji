package io.github.sakurawald.fuji.module.initializer.warning.service;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.service.paged_text.PagedMessageText;
import io.github.sakurawald.fuji.module.initializer.warning.WarningInitializer;
import io.github.sakurawald.fuji.module.initializer.warning.structure.PlayerWarnings;
import io.github.sakurawald.fuji.module.initializer.warning.structure.Warning;
import io.github.sakurawald.fuji.module.initializer.warning.structure.WarningRule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarningService {

    public static @NotNull PlayerWarnings getPlayerWarnings(@NotNull String playerName) {
        List<PlayerWarnings> players = WarningInitializer.data.model().players;
        Optional<PlayerWarnings> playerWarnings = players
            .stream()
            .filter(it -> it.getPlayer().equals(playerName))
            .findFirst();

        return playerWarnings
            .orElseGet(() -> {
                PlayerWarnings newValue = PlayerWarnings.make(playerName);
                players.add(newValue);
                WarningInitializer.data.writeStorage();
                return newValue;
            });
    }

    public static void createWarning(@NotNull String creatorName, @NotNull String targetPlayerName, @NotNull String warningDescription, @Nullable Long expirationTimestamp) {
        /* Create a new warning for the target player. */
        Warning newWarning = Warning.make(creatorName, warningDescription, expirationTimestamp);
        getPlayerWarnings(targetPlayerName)
            .getWarnings()
            .add(newWarning);
        WarningInitializer.data.writeStorage();

        /* Process the warning rules. */
        if (expirationTimestamp != null) {
            processTemporalWarningRules(targetPlayerName);
        } else {
            processPermanentWarningRules(targetPlayerName);
        }
    }

    public static void deleteWarning(@NotNull String targetPlayerName, @NotNull Warning warning) {
        getPlayerWarnings(targetPlayerName)
            .getWarnings()
            .remove(warning);
        WarningInitializer.data.writeStorage();
    }

    public static int clearWarnings(@NotNull String targetPlayerName) {
        List<Warning> warnings = getPlayerWarnings(targetPlayerName).getWarnings();
        int originalSize = warnings.size();
        warnings.clear();
        WarningInitializer.data.writeStorage();
        return originalSize;
    }

    public static void clearAllWarnings() {
        WarningInitializer.data.model().players = new ArrayList<>();
        WarningInitializer.data.writeStorage();
    }

    public static void processNotify(@NotNull ServerPlayerEntity targetPlayer, boolean isJoin) {
        /* Does the player have any warnings? */
        String playerName = PlayerHelper.getPlayerName(targetPlayer);
        List<Warning> activeWarnings = getPlayerWarnings(playerName)
            .getWarnings()
            .stream()
            .filter(Warning::isActive)
            .toList();
        if (activeWarnings.isEmpty()) return;

        /* Send notify to online staffs. */
        PlayerHelper.Lookup
            .getOnlinePlayers()
            .stream()
            .filter(it -> LuckpermsHelper.hasPermission(it.getUuid(), WarningInitializer.NOTIFY_WARNINGS_PERMISSION))
            .forEach(it -> {
                int activeWarningsSize = activeWarnings.size();
                if (isJoin) {
                    TextHelper.sendTextByKey(it, "warning.notify.join", playerName, activeWarningsSize);
                } else {
                    TextHelper.sendTextByKey(it, "warning.notify.leave", playerName, activeWarningsSize);
                }
            });
    }

    private static void processPermanentWarningRules(@NotNull String targetPlayerName) {
        processWarningRules(targetPlayerName, WarningInitializer.config.model().getOnPermanentWarningCreated(), Warning::isPermanentWarning);
    }

    private static void processTemporalWarningRules(@NotNull String targetPlayerName) {
        processWarningRules(targetPlayerName, WarningInitializer.config.model().getOnTemporalWarningCreated(), Warning::isTemporalWarning);
    }

    private static void processWarningRules(@NotNull String targetPlayerName, @NotNull List<WarningRule> warningRules, @NotNull Predicate<Warning> warningTypeFilter) {
        Optional<WarningRule> first = warningRules
            .stream()
            // Sort the higher value first.
            .sorted(Comparator
                .comparing(WarningRule::getIfNumberOfWarningsGreaterEqualThan)
                .reversed())
            .filter(warningRule -> {
                int numberOfWarnings = getPlayerWarnings(targetPlayerName)
                    .getWarnings()
                    .stream()
                    .filter(warning -> warningTypeFilter.test(warning) && warning.isActive())
                    .toList()
                    .size();
                return numberOfWarnings >= warningRule.getIfNumberOfWarningsGreaterEqualThan();
            })
            .findFirst();

        if (first.isPresent()) {
            WarningRule warningRule = first.get();

            /* Execute the warning rule. */
            LogUtil.info("Execute the warning rule for player {}: warning rule = {}", targetPlayerName, warningRule);

            // NOTE: Load the dummy offline server player entity, to provide the placeholder parsing context. (Use `/when-online` to execute commands on real server player entity.)
            ServerCommandSource offlineServerCommandSource = PlayerHelper.Loader
                .loadDummyPlayer(targetPlayerName)
                .getCommandSource();
            ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(offlineServerCommandSource);

            List<String> commands = warningRule.getCommands();
            CommandExecutor.executeBatch(extendedCommandSource, commands);
        }
    }

    public static void processWarningReminder(@NotNull ServerPlayerEntity targetPlayer) {
        var config = WarningInitializer.config.model().getWarningReminder();
        if (!config.isRemindWarnedPlayerOnJoinServer()) {
            return;
        }

        /* Filter all active warnings. */
        String playerName = PlayerHelper.getPlayerName(targetPlayer);
        PlayerWarnings playerWarnings = getPlayerWarnings(playerName);
        List<Warning> reminderSourceWarnings = playerWarnings
            .getWarnings()
            .stream()
            .filter(Warning::isActive)
            .filter(it -> (config.getReminderSource().isRemindPermanentWarningsType() && it.isPermanentWarning())
                || (config.getReminderSource().isRemindTemporalWarningsType() && it.isTemporalWarning()))
            .toList();
        if (reminderSourceWarnings.isEmpty()) return;

        /* Send the reminders to the target player. */
        TextHelper.sendTextByKey(targetPlayer, "warning.remind.header");
        PagedMessageText pagedMessageText = PagedMessageText.makePagedMessageText(targetPlayer, reminderSourceWarnings, 1, (entity, index, builder) -> {
            String description = entity.getDescription();
            String createdDate = ChronosUtil.Formatter.formatDate(entity.getCreatedTimestamp());
            String expirationDate = ChronosUtil.Formatter.formatDate(entity.getExpirationTimestamp());
            Text entityText = TextHelper.getTextByKey(targetPlayer, "warning.remind.entry", description, createdDate, expirationDate);
            builder.append(entityText);
        });
        pagedMessageText.sendPage(targetPlayer, 0);
    }
}
