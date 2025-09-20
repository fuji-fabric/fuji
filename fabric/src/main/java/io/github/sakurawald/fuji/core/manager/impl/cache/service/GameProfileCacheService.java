package io.github.sakurawald.fuji.core.manager.impl.cache.service;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.config.mapper.wrapper.GameProfileWrapper;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.service.gameprofile_fetcher.MojangProfileFetcher;
import java.time.Duration;
import java.util.function.Supplier;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class GameProfileCacheService {

    private static final String GAME_PROFILE_CACHE_KEY = "game_profile";

    public static void setGameProfileCache(@NotNull ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        GameProfileWrapper cachedGameProfile = getCachedGameProfile(player);
        LogUtil.debug("Set game profile cache for player {}. (cache = {})", playerName, cachedGameProfile);
    }

    public static @NotNull GameProfileWrapper getCachedGameProfile(@NotNull String onlinePlayerName) {
        return getCachedGameProfile(onlinePlayerName, Duration.ofDays(7), () -> supplyOnlineGameProfile(onlinePlayerName));
    }

    public static @NotNull GameProfileWrapper getCachedGameProfile(@NotNull ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        return getCachedGameProfile(playerName, Duration.ofMillis(0), () -> supplyOfflineGameProfile(player));
    }

    private static @NotNull GameProfileWrapper getCachedGameProfile(@NotNull String onlinePlayerName, @NotNull Duration expirationDuration, @NotNull Supplier<GameProfileWrapper> supplier) {
        return Managers
            .getCacheManager()
            .getCachedValueOrCompute(GAME_PROFILE_CACHE_KEY, onlinePlayerName, GameProfileWrapper.class, expirationDuration, supplier);
    }

    private static @NotNull GameProfileWrapper supplyOfflineGameProfile(@NotNull ServerPlayerEntity serverPlayerEntity) {
        return GameProfileWrapper.fromVanillaType(serverPlayerEntity.getGameProfile());
    }

    private static @NotNull GameProfileWrapper supplyOnlineGameProfile(@NotNull String onlinePlayerName) {
        return MojangProfileFetcher
            .fetchOnlinePlayerUUID(onlinePlayerName)
            .map(uuid -> {
                GameProfile gameProfile = new GameProfile(uuid, onlinePlayerName);

                return MojangProfileFetcher
                    .fetchOnlineGameProfile(onlinePlayerName)
                    .map(GameProfileWrapper::fromVanillaType)
                    .orElseGet(() -> GameProfileWrapper.fromVanillaType(gameProfile));
            })
            .orElseGet(() -> GameProfileWrapper.of(null, onlinePlayerName));
    }

}
