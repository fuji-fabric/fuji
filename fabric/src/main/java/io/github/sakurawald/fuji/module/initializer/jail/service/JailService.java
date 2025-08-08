package io.github.sakurawald.fuji.module.initializer.jail.service;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.service.date_parser.DateParser;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.jail.JailInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDataNode;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailRecord;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class JailService {

    public static List<JailDescriptor> getJailDescriptors() {
        return JailInitializer.config.model().getJailDescriptors();
    }

    public static List<String> getJailIds() {
        return getJailDescriptors()
            .stream()
            .map(JailDescriptor::getId)
            .toList();
    }

    public static Optional<JailDescriptor> findJailDescriptor(String id) {
        return getJailDescriptors()
            .stream()
            .filter(it -> it.getId().equals(id))
            .findFirst();
    }

    private static List<JailDataNode> getJailDataNodes() {
        return JailInitializer.data.model().getJailDataNodes();
    }

    private static <T> T withJailDataNode(@NotNull JailDescriptor jail, Function<JailDataNode, T> function) {
        Optional<JailDataNode> jailDataNode = getJailDataNodes()
            .stream()
            .filter(it -> it.getJailId().equals(jail.getId()))
            .findFirst();

        JailDataNode $jailDataNode = jailDataNode.orElseGet(() -> {
            JailDataNode newValue = JailDataNode.makeDefault(jail);
            getJailDataNodes().add(newValue);
            return newValue;
        });

        return function.apply($jailDataNode);
    }

    public static List<JailRecord> getJailRecords() {
        return getJailDescriptors()
            .stream()
            .flatMap(it -> getJailRecords(it).stream())
            .toList();
    }

    public static List<JailRecord> getEnabledRecords() {
        return filterEnabledJailRecords(getJailRecords());
    }

    public static List<JailRecord> getJailRecords(@NotNull JailDescriptor jailDescriptor) {
        return withJailDataNode(jailDescriptor, jailDataNode -> {
            List<JailRecord> jailRecords = jailDataNode.getRecords();
            jailRecords.forEach(jailRecord -> jailRecord.setOwnerJailDescriptor(jailDescriptor));
            return jailRecords;
        });
    }

    private static List<JailRecord> filterEnabledJailRecords(@NotNull List<JailRecord> jailRecords) {
        return jailRecords
            .stream()
            .filter(JailRecord::isEnable)
            .toList();
    }

    public static @NotNull List<String> getJailedPlayerNames() {
        return getEnabledRecords()
            .stream()
            .map(JailRecord::getPrisonerName)
            .toList();
    }

    public static Optional<JailRecord> getActiveJailRecord(String playerName) {
        return getEnabledRecords()
            .stream()
            .filter(jailRecord -> jailRecord.getPrisonerName().equals(playerName))
            .findFirst();
    }

    public static void createJailDescriptor(@NotNull String jailId, @NotNull ServerCommandSource source) {
        GlobalPos globalPos = GlobalPos.of(source);
        JailDescriptor newValue = JailDescriptor.make(jailId, globalPos);
        JailInitializer.config.model().getJailDescriptors().add(newValue);
        JailInitializer.config.writeStorage();
    }

    public static void deleteJailDescriptor(@NotNull JailDescriptor jailDescriptor) {
        JailInitializer.config.model().getJailDescriptors().remove(jailDescriptor);
        JailInitializer.config.writeStorage();
    }

    public static void setJailPosition(@NotNull JailDescriptor jailDescriptor, @NotNull GlobalPos globalPos) {
        jailDescriptor.setGlobalPosition(globalPos);
        JailInitializer.config.writeStorage();
    }

    public static void createJailRecord(String creatorName, String playerName, JailDescriptor jail, String reason, String $duration) {
        withJailDataNode(jail, jailDataNode -> {
            int specifiedJailSeconds = DateParser.parseIntoSeconds($duration);
            JailRecord jailRecord = JailRecord.make(creatorName, playerName, specifiedJailSeconds, reason);
            jailDataNode.getRecords().add(jailRecord);
            return null;
        });

        // NOTE: Execute the commands later, to ensure the `active jail record` can be retried by the placeholders.
        executeOnJailedCommands(jail, playerName);

        // Update display names.
        PlayerHelper.updateDisplayNames();
    }

    private static void executeOnUnjailedCommands(JailDescriptor jail, String playerName) {
        ServerPlayerEntity offlinePlayerEntity = PlayerHelper.Loader.loadDummyPlayer(playerName);
        CommandExecutor.execute(ExtendedCommandSource.asConsole(offlinePlayerEntity.getCommandSource()), jail.getEvents().getOnUnjailedEvent());
    }

    public static void deactivateJailRecord(JailRecord jailRecord) {
        // NOTE: Execute the commands first, to ensure the `active jail record` can be retried by the placeholders.
        executeOnUnjailedCommands(jailRecord.getOwnerJailDescriptor(), jailRecord.getPrisonerName());

        jailRecord.setEnable(false);

        // Update display names.
        PlayerHelper.updateDisplayNames();
    }

    public static void deactivateJailRecordWithoutEvents(String playerName) {
        getActiveJailRecord(playerName)
            .ifPresent(jailRecord -> jailRecord.setEnable(false));
    }

    private static void executeOnJailedCommands(JailDescriptor jail, String playerName) {
        ServerPlayerEntity offlinePlayerEntity = PlayerHelper.Loader.loadDummyPlayer(playerName);
        CommandExecutor.execute(ExtendedCommandSource.asConsole(offlinePlayerEntity.getCommandSource()), jail.getEvents().getOnJailedEvent());
    }

    public static void updateJailRecords(int passedTimeInMillSeconds) {
        getEnabledRecords()
            .forEach(jailRecord -> jailRecord.onUpdateRecord(passedTimeInMillSeconds));
    }

    public static void executePatrolCommands(JailDescriptor jail) {
        List<JailRecord> enabledJailRecords = filterEnabledJailRecords(getJailRecords(jail));
        enabledJailRecords
            .forEach(jailRecord -> ServerHelper.executeSync(() -> {
                String playerName = jailRecord.getPrisonerName();
                PlayerHelper.Lookup.getOnlinePlayerByName(playerName)
                    .ifPresent(onlinePlayer -> {
                        LogUtil.debug("Execute patrol commands: jail = {}, jailedPlayer = {}", jail.getId(), playerName);
                        List<String> patrolCommands = jail.getPatrol().getPatrolCommands();
                        CommandExecutor.execute(ExtendedCommandSource.asConsole(onlinePlayer.getCommandSource()), patrolCommands);
                    });
            }));
    }

    public static Text getNoJailStatusText() {
        String value = JailInitializer.config.model().getNoJailStatusText();
        return TextHelper.getTextByValue(null, value);
    }

    public static @NotNull Text modifyDisplayName(@NotNull Text original, @NotNull PlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        return getActiveJailRecord(playerName)
            .map(it -> TextHelper.getTextByValue(player, JailInitializer.config.model().getJailedPlayerTabListText()))
            .orElse(original);
    }
}
