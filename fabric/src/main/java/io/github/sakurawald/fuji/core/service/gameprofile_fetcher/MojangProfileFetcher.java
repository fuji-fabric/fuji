package io.github.sakurawald.fuji.core.service.gameprofile_fetcher;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.auxiliary.HttpUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;


public class MojangProfileFetcher {

    private static final String API_SERVER = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern UUID_CONVERTER_PATTERN = Pattern.compile("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)");

    public static Optional<GameProfile> fetchOnlineGameProfile(@NotNull String playerName) {
        return fetchOnlinePlayerUUID(playerName)
            .map(uuid -> new GameProfile(uuid, playerName));
    }

    public static Optional<UUID> fetchOnlinePlayerUUID(@NotNull String playerName) {
        String rawUUID;
        try {
            rawUUID = JsonParser.parseString(HttpUtil.sendGetRequest(API_SERVER + playerName)).getAsJsonObject().get("id").getAsString();
        } catch (IOException e) {
            LogUtil.debug("Failed to fetch online uuid from mojang server for {}", playerName);
            return Optional.empty();
        }

        UUID value = UUID.fromString(UUID_CONVERTER_PATTERN.matcher(rawUUID).replaceFirst("$1-$2-$3-$4-$5"));
        return Optional.of(value);
    }

}
