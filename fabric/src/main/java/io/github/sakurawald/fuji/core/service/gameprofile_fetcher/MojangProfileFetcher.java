package io.github.sakurawald.fuji.core.service.gameprofile_fetcher;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.auxiliary.HttpUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.AuthlibHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;


public class MojangProfileFetcher {

    private static final String API_SERVER = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern UUID_CONVERTER_PATTERN = Pattern.compile("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)");

    public static Optional<GameProfile> fetchOnlineGameProfile(@NotNull String playerName) {
        return fetchOnlinePlayerUUID(playerName)
            .map(uuid -> new GameProfile(uuid, playerName))
            .map(gameProfile -> {
                MojangSkinProvider
                    .fetchSkin(playerName)
                    .ifPresent(property -> AuthlibHelper.modifyGameProfile(gameProfile, property));
                return gameProfile;
            });
    }

    public static Optional<UUID> fetchOnlinePlayerUUID(@NotNull String playerName) {
        UserCache userCache = ServerHelper.getServer().getUserCache();
        if (userCache == null) {
            return fetchOnlinePlayerUUID$Fallback(playerName);
        }

        try {
            return PlayerHelper.Cache
                .getOfflineGameProfileByName(playerName)
                .map(GameProfile::getId)
                .or(() -> fetchOnlinePlayerUUID$Fallback(playerName));
        } catch (Exception e) {
            return fetchOnlinePlayerUUID$Fallback(playerName);
        }
    }

    private static Optional<UUID> fetchOnlinePlayerUUID$Fallback(@NotNull String playerName) {
        String rawUUID;
        try {
            String json = HttpUtil.sendGetRequest(API_SERVER + playerName);
            rawUUID = JsonParser.parseString(json).getAsJsonObject().get("id").getAsString();
        } catch (IOException e) {
            LogUtil.debug("Failed to fetch online uuid from mojang server for {}", playerName);
            return Optional.empty();
        }

        UUID value = UUID.fromString(UUID_CONVERTER_PATTERN.matcher(rawUUID).replaceFirst("$1-$2-$3-$4-$5"));
        return Optional.of(value);
    }

}
