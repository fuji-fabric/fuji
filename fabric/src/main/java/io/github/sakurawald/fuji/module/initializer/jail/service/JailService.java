package io.github.sakurawald.fuji.module.initializer.jail.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.service.date_parser.DateParser;
import io.github.sakurawald.fuji.module.initializer.jail.JailInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDataNode;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailRecord;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.network.ServerPlayerEntity;
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

    private static synchronized List<JailDataNode> getJailDataNodes() {
        return JailInitializer.data.model().getJailDataNodes();
    }

    public static List<JailRecord> getJailRecords() {
        return getJailDescriptors()
            .stream()
            .flatMap(it -> getJailRecords(it).stream())
            .toList();
    }

    public static List<JailRecord> getEnabledRecords() {
        return getJailRecords()
            .stream()
            .filter(JailRecord::isEnable)
            .toList();
    }

    public static List<JailRecord> getJailRecords(@NotNull JailDescriptor jailDescriptor) {
        return withJailDataNode(jailDescriptor, jailDataNode -> {
            List<JailRecord> jailRecords = jailDataNode.getRecords();
            jailRecords.forEach(jailRecord -> jailRecord.setOwnerJailDescriptor(jailDescriptor));
            return jailRecords;
        });
    }

    public static @NotNull List<String> getJailedPlayerNames() {
        return getEnabledRecords()
            .stream()
            .map(JailRecord::getPlayerName)
            .toList();
    }

    public static Optional<JailRecord> getCurrentJailRecord(String playerName) {
        return getEnabledRecords()
            .stream()
            .filter(jailRecord -> jailRecord.getPlayerName().equals(playerName))
            .findFirst();
    }

    public static void createJailRecord(String creatorName, String playerName, JailDescriptor jail, String reason, String $duration) {
        withJailDataNode(jail, jailDataNode -> {
            int specifiedJailSeconds = DateParser.parseAccumulatedSeconds($duration);
            JailRecord jailRecord = JailRecord.make(creatorName, playerName, specifiedJailSeconds, reason);
            jailDataNode.getRecords().add(jailRecord);
            return null;
        });
        executeOnJailedCommands(jail, playerName);
    }

    private static void executeOnUnjailedCommands(JailDescriptor jail, String playerName) {
        ServerPlayerEntity offlinePlayerEntity = PlayerHelper.loadServerPlayerEntity(playerName);
        CommandExecutor.execute(ExtendedCommandSource.asConsole(offlinePlayerEntity.getCommandSource()), jail.getEvents().getOnUnjailedEvent());
    }

    public static void pardonJailRecord(JailRecord jailRecord) {
        jailRecord.setEnable(false);
        executeOnUnjailedCommands(jailRecord.getOwnerJailDescriptor(), jailRecord.getPlayerName());
    }

    private static void executeOnJailedCommands(JailDescriptor jail, String playerName) {
        ServerPlayerEntity offlinePlayerEntity = PlayerHelper.loadServerPlayerEntity(playerName);
        CommandExecutor.execute(ExtendedCommandSource.asConsole(offlinePlayerEntity.getCommandSource()), jail.getEvents().getOnJailedEvent());
    }

    public static void updateJailRecords(int passedTimeInMillSeconds) {
        getEnabledRecords()
            .forEach(jailRecord -> jailRecord.onUpdateRecord(passedTimeInMillSeconds));
    }

}
