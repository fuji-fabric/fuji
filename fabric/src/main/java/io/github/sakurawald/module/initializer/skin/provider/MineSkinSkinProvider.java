package io.github.sakurawald.module.initializer.skin.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.core.auxiliary.IOUtil;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.module.initializer.skin.structure.SkinVariant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MineSkinSkinProvider {

    private static final String API_ENDPOINT = "https://api.mineskin.org/generate/url";

    public static @Nullable Property fetchSkin(String url, @NotNull SkinVariant variant) {
        try {
            String param = "{\"variant\":\"%s\",\"name\":\"%s\",\"visibility\":%d,\"url\":\"%s\"}"
                .formatted(variant.toString(), "none", 0, url);
            String json = IOUtil.requestPost(API_ENDPOINT, param);

            JsonObject texture = JsonParser.parseString(json)
                .getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonObject("texture");

            return new Property("textures", texture.get("value").getAsString(), texture.get("signature").getAsString());
        } catch (IOException e) {
            LogUtil.debug("failed to fetch skin from mine-skin server: url = {}", url);
        }

        return null;
    }
}
