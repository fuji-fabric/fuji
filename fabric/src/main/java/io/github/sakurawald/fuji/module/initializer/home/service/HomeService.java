package io.github.sakurawald.fuji.module.initializer.home.service;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.home.HomeInitializer;
import io.github.sakurawald.fuji.module.initializer.home.command.argument.wrapper.HomeName;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class HomeService {

    public static @NotNull BiMap<String, GlobalPos> withHomeMap(@NotNull ServerPlayerEntity player) {
        return withHomeMap(PlayerHelper.getPlayerName(player));
    }

    public static @NotNull BiMap<String, GlobalPos> withHomeMap(@NotNull String playerName) {
        return HomeInitializer.data.model().getName2home()
            .computeIfAbsent(playerName, k -> HashBiMap.create());
    }

    public static Optional<GlobalPos> findHome(@NotNull String playerName, @NotNull String homeName) {
        return Optional
            .ofNullable(withHomeMap(playerName)
            .get(homeName));
    }

    public static void ensureHomeNameExisting(@NotNull ServerPlayerEntity player, @NotNull HomeName homeName) {
        String playerName = PlayerHelper.getPlayerName(player);
        String homeNameString = homeName.getValue();
        var unused = findHome(playerName, homeNameString)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(player, "home.not_found", homeName);
                return new AbortCommandExecutionException();
            });
    }

    public static void removeHome(@NotNull String playerName, @NotNull String homeName) {
        BiMap<String, GlobalPos> homes = withHomeMap(playerName);
        homes.remove(homeName);
    }
}
