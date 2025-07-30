package io.github.sakurawald.fuji.core.service.gameprofile_fetcher;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.auxiliary.HttpUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;


public class MojangProfileFetcher {

    private static final String API_SERVER = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern UUID_CONVERTER_PATTERN = Pattern.compile("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)");

    public static GameProfile makeOnlineGameProfile(String playerName) {
        return new GameProfile(fetchOnlineUUID(playerName), playerName);
    }

    public static @Nullable UUID fetchOnlineUUID(String playerName) {
        String rawUUID;
        try {
            rawUUID = JsonParser.parseString(HttpUtil.sendGetRequest(API_SERVER + playerName)).getAsJsonObject().get("id").getAsString();
        } catch (IOException e) {
            LogUtil.debug("Failed to fetch online uuid from mojang server for {}", playerName);
            return null;
        }
        return UUID.fromString(UUID_CONVERTER_PATTERN.matcher(rawUUID).replaceFirst("$1-$2-$3-$4-$5"));
    }

}
