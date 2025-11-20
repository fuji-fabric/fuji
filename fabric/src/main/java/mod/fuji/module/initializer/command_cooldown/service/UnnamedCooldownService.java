package mod.fuji.module.initializer.command_cooldown.service;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.structure.Cooldown;
import mod.fuji.module.initializer.command_cooldown.CommandCooldownInitializer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class UnnamedCooldownService {

    private static final Map<String, Cooldown<String>> playerName2UnnamedCooldown = new HashMap<>();

    public static Map<String, Long> listUnnamedCooldowns() {
        return CommandCooldownInitializer.config.model().getUnnamedCooldown();
    }

    public static long computeRemainingUnnamedCooldownDuration(@NotNull ServerPlayer player, @NotNull String commandLine) {
        String key = PlayerHelper.getPlayerName(player);
        Cooldown<String> unnamedCooldown = playerName2UnnamedCooldown.computeIfAbsent(key, k -> new Cooldown<>());

        Optional<Map.Entry<String, Long>> matchedUnnamedCooldown = listUnnamedCooldowns()
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
