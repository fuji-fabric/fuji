package io.github.sakurawald.fuji.module.initializer.command_cooldown.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.CommandCooldownInitializer;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.command.argument.wrapper.CommandCooldownName;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCommandCooldown;
import java.util.List;
import java.util.Map;
import net.minecraft.server.network.ServerPlayerEntity;

public class NamedCooldownService {

    public static Map<String, NamedCommandCooldown> getNamedCooldownList() {
        return CommandCooldownInitializer.config.model().namedCooldown.list;
    }

    public static void deleteNamedCooldown(CommandCooldownName name) {
        String $name = name.getValue();
        getNamedCooldownList().remove($name);
        CommandCooldownInitializer.config.writeStorage();
    }

    public static void createNamedCooldown(String name, long cooldownDuration, int $maxUses, Boolean $persistent, Boolean $global) {
        NamedCommandCooldown namedCommandCooldown = NamedCommandCooldown.makeNamedCooldown(name, cooldownDuration, $maxUses, $persistent, $global);
        getNamedCooldownList().put(name, namedCommandCooldown);
        CommandCooldownInitializer.config.writeStorage();
    }

    public static int testNamedCooldown(NamedCommandCooldown cooldown, ServerPlayerEntity player, List<String> onSuccessCommands, List<String> onFailureCommands) {
        String key = NamedCommandCooldown.toKey(player);

        /* If failed. */
        long remainingDuration = cooldown.tryUse(key, cooldown.getCooldownDuration());
        int uses = cooldown.getUses().computeIfAbsent(key, k -> 0);
        int availableUses = cooldown.getMaxUses() - uses;
        if (remainingDuration > 0 || availableUses <= 0) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), onFailureCommands);
            return CommandHelper.Return.FAIL;
        }

        /* If succeeded. */
        cooldown.getUses().compute(key, (k, v) -> v == null ? 1 : v + 1);
        CommandCooldownInitializer.config.writeStorage();

        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), onSuccessCommands);
        return CommandHelper.Return.SUCCESS;
    }

    public static void resetNamedCooldownDuration(CommandCooldownName name, String key) {
        NamedCommandCooldown namedCommandCooldown = getNamedCooldownList().get(name.getValue());
        namedCommandCooldown.getTimestamp().put(key, 0L);
        CommandCooldownInitializer.config.writeStorage();
    }
}
