package io.github.sakurawald.core.service.gameprofile_fetcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.core.auxiliary.IOUtil;
import io.github.sakurawald.core.auxiliary.LogUtil;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;


@UtilityClass
public class MojangProfileFetcher {

    private static final String API_SERVER = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_SERVER = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Pattern UUID_CONVERTER_PATTERN = Pattern.compile("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)");

    @SuppressWarnings("DataFlowIssue")
    public static GameProfile makeOnlineGameProfile(String playerName) {
        return new GameProfile(fetchOnlineUUID(playerName), playerName);
    }

    public static @Nullable UUID fetchOnlineUUID(String playerName) {
        String rawUUID;
        try {
            rawUUID = JsonParser.parseString(IOUtil.requestGet(API_SERVER + playerName)).getAsJsonObject().get("id").getAsString();
        } catch (IOException e) {
            LogUtil.debug("failed to fetch online uuid from mojang server for {}", playerName);
            return null;
        }
        return UUID.fromString(UUID_CONVERTER_PATTERN.matcher(rawUUID).replaceFirst("$1-$2-$3-$4-$5"));
    }

    public static @Nullable Property fetchOnlineSkin(String playerName) {
        try {
            UUID uuid = fetchOnlineUUID(playerName);
            String json = IOUtil.requestGet(SESSION_SERVER + uuid + "?unsigned=false");

            JsonObject texture = JsonParser.parseString(json)
                .getAsJsonObject()
                .getAsJsonArray("properties")
                .get(0).getAsJsonObject();

            return new Property("textures", texture.get("value").getAsString(), texture.get("signature").getAsString());
        } catch (Exception e) {
            LogUtil.debug("failed to fetch online skin from mojang server for {}", playerName);
        }
        return null;
    }
}
