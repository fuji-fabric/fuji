package io.github.sakurawald.fuji.module.initializer.command_cooldown.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.structure.Cooldown;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.CommandCooldownInitializer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class UnnamedCooldownService {

    private static final Map<String, Cooldown<String>> playerName2UnnamedCooldown = new HashMap<>();

    public static long computeRemainingUnnamedCooldownDuration(@NotNull ServerPlayerEntity player, @NotNull String commandLine) {
        String key = PlayerHelper.getPlayerName(player);
        Cooldown<String> unnamedCooldown = playerName2UnnamedCooldown.computeIfAbsent(key, k -> new Cooldown<>());

        Optional<Map.Entry<String, Long>> matchedUnnamedCooldown = CommandCooldownInitializer.config.model().unnamed_cooldown
            .entrySet()
            .stream()
            .filter(it -> commandLine.matches(it.getKey()))
            .findFirst();

        return matchedUnnamedCooldown
            .map(entry -> unnamedCooldown.tryUse(entry.getKey(), entry.getValue()))
            // NOTE: If there is no an unnamed cooldown defined for this command line, then we return 0 as the remaining cooldown duration.
            .orElse(0L);
    }
}
