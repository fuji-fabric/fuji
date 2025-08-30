package io.github.sakurawald.fuji.module.initializer.skin.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.HttpUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.service.gameprofile_fetcher.MojangProfileFetcher;
import java.io.IOException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class MojangSkinProvider {

    private static final String MOJANG_SESSION_SERVER = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static Optional<Property> fetchSkin(@NotNull String playerName) {
        try {
            return MojangProfileFetcher
                .fetchOnlinePlayerUUID(playerName)
                .flatMap(uuid -> {
                    String responseJsonString;
                    try {
                        responseJsonString = HttpUtil.sendGetRequest(MOJANG_SESSION_SERVER + uuid + "?unsigned=false");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    JsonObject textureJsonObject = JsonParser
                        .parseString(responseJsonString)
                        .getAsJsonObject()
                        .getAsJsonArray("properties")
                        .get(0).getAsJsonObject();
                    String value = textureJsonObject.get("value").getAsString();
                    String signature = textureJsonObject.get("signature").getAsString();
                    return Optional.of(new Property("textures", value, signature));
                });
        } catch (Exception e) {
            LogUtil.debug("Failed to fetch online skin from Mojang server: playerName = {}", playerName);
            return Optional.empty();
        }
    }
}
