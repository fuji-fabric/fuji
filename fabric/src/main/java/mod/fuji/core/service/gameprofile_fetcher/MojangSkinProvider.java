package mod.fuji.core.service.gameprofile_fetcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import mod.fuji.core.auxiliary.ExceptionUtil;
import mod.fuji.core.auxiliary.HttpUtil;
import mod.fuji.core.auxiliary.LogUtil;
import java.io.IOException;
import java.util.Optional;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import org.jetbrains.annotations.NotNull;

public class MojangSkinProvider {

    private static final String MOJANG_SESSION_SERVER = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static Optional<Property> fetchSkin(@NotNull String onlinePlayerName) {
        try {
            return MojangProfileFetcher
                .fetchOnlinePlayerUUID(onlinePlayerName)
                .flatMap(uuid -> {
                    String responseJsonString;
                    try {
                        responseJsonString = HttpUtil.sendGetRequest(MOJANG_SESSION_SERVER + uuid + "?unsigned=false");
                    } catch (IOException e) {
                        throw ExceptionUtil.makeReThrownException(e);
                    }
                    JsonObject textureJsonObject = JsonParser
                        .parseString(responseJsonString)
                        .getAsJsonObject()
                        .getAsJsonArray("properties")
                        .get(0).getAsJsonObject();
                    String value = textureJsonObject.get("value").getAsString();
                    String signature = textureJsonObject.get("signature").getAsString();
                    return Optional.of(new Property(AuthlibHelper.TEXTURES_PROPERTY_KEY, value, signature));
                });
        } catch (Exception e) {
            LogUtil.debug("Failed to fetch online skin from Mojang server: playerName = {}", onlinePlayerName);
            return Optional.empty();
        }
    }
}
