package io.github.sakurawald.fuji.module.initializer.command_cooldown.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.CommandCooldownInitializer;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCooldownDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCooldownDataNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class NamedCooldownService {

    public static Map<String, NamedCooldownDescriptor> getNamedCooldownDescriptors() {
        return CommandCooldownInitializer.config.model().namedCooldown.list;
    }

    public static Optional<NamedCooldownDescriptor> findNamedCooldownDescriptor(@NotNull String id) {
        return getNamedCooldownDescriptors()
            .entrySet()
            .stream()
            .filter(it -> it.getKey().equals(id))
            .findFirst()
            .map(Map.Entry::getValue);
    }

    public static void createNamedCooldownDescriptor(@NotNull String name, long cooldownDuration, int maxUses, boolean persistent, boolean global) {
        NamedCooldownDescriptor namedCooldownDescriptor = NamedCooldownDescriptor.make(name, cooldownDuration, maxUses, persistent, global);
        getNamedCooldownDescriptors().put(name, namedCooldownDescriptor);
        CommandCooldownInitializer.config.writeStorage();
    }

    public static void deleteNamedCooldownDescriptor(@NotNull NamedCooldownDescriptor descriptor) {
        getNamedCooldownDescriptors().remove(descriptor.getName());
        CommandCooldownInitializer.config.writeStorage();
    }

    public static <T> T withNamedCooldownDataNode(@NotNull NamedCooldownDescriptor namedCooldownDescriptor, @NotNull Function<NamedCooldownDataNode, T> function) {
        Optional<NamedCooldownDataNode> first = getNamedCooldownNodes()
            .stream()
            .filter(it -> it.getId().equals(namedCooldownDescriptor.getName()))
            .findFirst();

        NamedCooldownDataNode namedCooldownDataNode = first.orElseGet(() -> {
            NamedCooldownDataNode newValue = new NamedCooldownDataNode();
            newValue.setId(namedCooldownDescriptor.getName());
            getNamedCooldownNodes().add(newValue);
            return newValue;
        });

        namedCooldownDataNode.setDescriptor(namedCooldownDescriptor);
        return function.apply(namedCooldownDataNode);
    }

    private static List<NamedCooldownDataNode> getNamedCooldownNodes() {
        return CommandCooldownInitializer.namedCooldownData.model()
            .getNodes();
    }

    public static int testNamedCooldown(@NotNull NamedCooldownDescriptor descriptor, @NotNull ServerPlayerEntity player, @NotNull List<String> onSuccessCommands, @NotNull List<String> onFailureCommands) {
        String key = NamedCooldownDataNode.toKey(player);

        return withNamedCooldownDataNode(descriptor, dataNode -> {
            /* If failed. */
            long remainingDuration = dataNode.tryUse(key, descriptor.getCooldownDuration());
            int uses = dataNode.getUses().computeIfAbsent(key, k -> 0);
            int availableUses = descriptor.getMaxUses() - uses;
            if (remainingDuration > 0 || availableUses <= 0) {
                CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), onFailureCommands);
                return CommandHelper.Return.FAILURE;
            }

            /* If succeeded. */
            dataNode.getUses().compute(key, (k, v) -> v == null ? 1 : v + 1);
            CommandCooldownInitializer.config.writeStorage();

            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), onSuccessCommands);
            return CommandHelper.Return.SUCCESS;
        });
    }

    public static void resetNamedCooldownDuration(@NotNull NamedCooldownDescriptor descriptor, @NotNull String key) {
        withNamedCooldownDataNode(descriptor, dataNode -> {
            dataNode.getCooldown().getTimestamp().put(key, 0L);
            return null;
        });

    }
}
