package mod.fuji.core.service.cache.service;

import com.mojang.authlib.GameProfile;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.config.mapper.representation.GameProfileIR;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerJoinedEvent;
import mod.fuji.core.service.cache.CacheManager;
import mod.fuji.core.service.gameprofile_fetcher.MojangProfileFetcher;
import java.time.Duration;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class GameProfileCacheService {

    private static final String GAME_PROFILE_CACHE_KEY = "game_profile";

    @EventConsumer
    private static void updateGameProfileCache(PlayerJoinedEvent event) {
        @NotNull ServerPlayer player = event.getPlayer();
        String playerName = PlayerHelper.getPlayerName(player);
        GameProfileIR cachedGameProfile = getCachedGameProfile(player);
        LogUtil.debug("Update game profile cache: player = {}, value = {}", playerName, cachedGameProfile);
    }

    public static @NotNull GameProfileIR getCachedGameProfile(@NotNull String onlinePlayerName) {
        return getCachedGameProfile(onlinePlayerName, Duration.ofDays(7), () -> supplyOnlineGameProfile(onlinePlayerName));
    }

    private static @NotNull GameProfileIR getCachedGameProfile(@NotNull ServerPlayer player) {
        String playerName = PlayerHelper.getPlayerName(player);
        return getCachedGameProfile(playerName, Duration.ofMillis(0), () -> supplyOfflineGameProfile(player));
    }

    private static @NotNull GameProfileIR getCachedGameProfile(@NotNull String onlinePlayerName, @NotNull Duration expirationDuration, @NotNull Supplier<GameProfileIR> supplier) {
        return CacheManager
            .getCachedValueOrCompute(GAME_PROFILE_CACHE_KEY, onlinePlayerName, GameProfileIR.class, expirationDuration, supplier);
    }

    private static @NotNull GameProfileIR supplyOfflineGameProfile(@NotNull ServerPlayer serverPlayerEntity) {
        return GameProfileIR.from(serverPlayerEntity.getGameProfile());
    }

    private static @NotNull GameProfileIR supplyOnlineGameProfile(@NotNull String onlinePlayerName) {
        return MojangProfileFetcher
            .fetchOnlinePlayerUUID(onlinePlayerName)
            .map(uuid -> {
                GameProfile gameProfile = new GameProfile(uuid, onlinePlayerName);

                return MojangProfileFetcher
                    .fetchOnlineGameProfile(onlinePlayerName)
                    .map(GameProfileIR::from)
                    .orElseGet(() -> GameProfileIR.from(gameProfile));
            })
            .orElseGet(() -> GameProfileIR.from(null, onlinePlayerName));
    }

}
