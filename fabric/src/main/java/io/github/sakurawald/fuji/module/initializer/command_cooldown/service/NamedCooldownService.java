package io.github.sakurawald.fuji.module.initializer.command_cooldown.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyStringList;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.StringList;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.CommandCooldownInitializer;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.command.argument.wrapper.CommandCooldownName;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCommandCooldown;
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
        NamedCommandCooldown namedCommandCooldown = new NamedCommandCooldown(name, cooldownDuration, $maxUses, $persistent, $global);
        getNamedCooldownList().put(name, namedCommandCooldown);
        CommandCooldownInitializer.config.writeStorage();
    }

    public static int testNamedCooldown(ServerPlayerEntity player, GreedyStringList onSuccess, NamedCommandCooldown cooldown, String key, StringList $onFailed) {
        long remainingTime = cooldown.tryUse(key, cooldown.getCooldownDuration());
        int usage = cooldown.getUses().getOrDefault(key, 0);
        int leftUsage = cooldown.getMaxUses() - usage;
        if (remainingTime > 0 || leftUsage <= 0) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), $onFailed.getValue());
            return CommandHelper.Return.FAIL;
        }

        cooldown.getUses().compute(key, (k, v) -> v == null ? 1 : v + 1);
        CommandCooldownInitializer.config.writeStorage();
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), onSuccess.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    public static void resetNamedCooldownDuration(CommandCooldownName name, String key) {
        NamedCommandCooldown namedCommandCooldown = getNamedCooldownList().get(name.getValue());
        namedCommandCooldown.getTimestamp().put(key, 0L);
        CommandCooldownInitializer.config.writeStorage();
    }
}
