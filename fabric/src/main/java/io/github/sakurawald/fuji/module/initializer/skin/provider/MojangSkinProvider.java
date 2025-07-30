package io.github.sakurawald.fuji.module.initializer.skin.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.HttpUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.service.gameprofile_fetcher.MojangProfileFetcher;
import java.util.Optional;
import java.util.UUID;

public class MojangSkinProvider {

    private static final String MOJANG_SESSION_SERVER = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static Optional<Property> fetchSkin(String playerName) {
        try {
            UUID onlinePlayerUUID = MojangProfileFetcher.fetchOnlineUUID(playerName);
            String responseJsonString = HttpUtil.sendGetRequest(MOJANG_SESSION_SERVER + onlinePlayerUUID + "?unsigned=false");
            JsonObject textureJsonObject = JsonParser
                .parseString(responseJsonString)
                .getAsJsonObject()
                .getAsJsonArray("properties")
                .get(0).getAsJsonObject();
            String value = textureJsonObject.get("value").getAsString();
            String signature = textureJsonObject.get("signature").getAsString();
            return Optional.of(new Property("textures", value, signature));
        } catch (Exception e) {
            LogUtil.debug("Failed to fetch online skin from Mojang server: playerName = {}", playerName);
        }
        return Optional.empty();
    }
}
