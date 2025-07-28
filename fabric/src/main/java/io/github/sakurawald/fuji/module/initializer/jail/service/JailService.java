package io.github.sakurawald.fuji.module.initializer.jail.service;

import io.github.sakurawald.fuji.core.service.date_parser.DateParser;
import io.github.sakurawald.fuji.module.initializer.jail.JailInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDataNode;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailRecord;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
        Optional<JailDataNode> jailDataNode = JailInitializer.data.model()
            .getJailDataNodes()
            .stream()
            .filter(it -> it.getJailId().equals(jail.getId()))
            .findFirst();

        JailDataNode $jailDataNode = jailDataNode.orElseGet(() -> {
            JailDataNode newValue = JailDataNode.makeDefault(jail);
            JailInitializer.data.model().getJailDataNodes().add(newValue);
            return newValue;
        });

        T apply = function.apply($jailDataNode);
        JailInitializer.data.writeStorage();
        return apply;
    }

    public static boolean isInJail(@NotNull JailDescriptor jail, String playerName) {
        return withJailDataNode(jail, jailDataNode -> jailDataNode
            .getRecords()
            .stream()
            .anyMatch(it -> it.getPlayerName().equals(playerName)));
    }

    public static Optional<JailDescriptor> getCurrentJailDescriptor(String playerName) {
        return getJailDescriptors()
            .stream()
            .filter(jail -> isInJail(jail, playerName))
            .findFirst();
    }

    public static void createJailRecord(String creatorName, String playerName, JailDescriptor jail, String reason, String $duration) {
        withJailDataNode(jail, jailDataNode -> {
            int specifiedJailSeconds = DateParser.parseAccumulatedSeconds($duration);
            JailRecord jailRecord = JailRecord.make(creatorName, playerName, specifiedJailSeconds, reason);
            jailDataNode.getRecords().add(jailRecord);
            return null;
        });
    }

}
