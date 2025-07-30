package io.github.sakurawald.fuji.module.initializer.skin.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.HttpUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinVariant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MineSkinSkinProvider {

    private static final String API_ENDPOINT = "https://api.mineskin.org/generate/url";

    public static Optional<Property> fetchSkin(@NotNull String skinImageURL, @NotNull SkinVariant skinVariant) {
        try {
            String param = "{\"variant\":\"%s\",\"name\":\"%s\",\"visibility\":%d,\"url\":\"%s\"}"
                .formatted(skinVariant.toString(), "none", 0, skinImageURL);
            String responseJson = HttpUtil.sendPostRequest(API_ENDPOINT, param);

            JsonObject textureJsonObject = JsonParser
                .parseString(responseJson)
                .getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonObject("texture");
            String value = textureJsonObject.get("value").getAsString();
            String signature = textureJsonObject.get("signature").getAsString();
            return Optional.of(new Property("textures", value, signature));
        } catch (IOException e) {
            LogUtil.debug("Failed to fetch skin from mine-skin server: url = {}", skinImageURL);
        }

        return Optional.empty();
    }
}
