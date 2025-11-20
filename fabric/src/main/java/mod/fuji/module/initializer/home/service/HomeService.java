package mod.fuji.module.initializer.home.service;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.home.HomeInitializer;
import mod.fuji.module.initializer.home.command.argument.wrapper.HomeName;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class HomeService {

    public static @NotNull BiMap<String, GlobalPos> withHomeMap(@NotNull ServerPlayer player) {
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

    public static void ensureHomeNameExisting(@NotNull ServerPlayer player, @NotNull HomeName homeName) {
        String playerName = PlayerHelper.getPlayerName(player);
        String homeNameString = homeName.getValue();
        var unused = findHome(playerName, homeNameString)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(player, "home.not_found", homeName);
                return new AbortCommandExecutionException();
            });
    }

    public static void renameHome(@NotNull String playerName, @NotNull String oldName, String newName) {
        BiMap<String, GlobalPos> homes = withHomeMap(playerName);
        GlobalPos value = homes.get(oldName);
        homes.remove(oldName);
        homes.put(newName, value);
    }

    public static void removeHome(@NotNull String playerName, @NotNull String homeName) {
        BiMap<String, GlobalPos> homes = withHomeMap(playerName);
        homes.remove(homeName);
    }
}
