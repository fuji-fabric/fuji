package io.github.sakurawald.fuji.core.config.migrator.version;

import com.google.gson.JsonElement;
import io.github.sakurawald.fuji.Fuji;
import org.jetbrains.annotations.NotNull;

public class VersionPropertyInjector {

    public static final String MOD_VERSION_KEY = "MOD_VERSION";

    public static void injectVersionProperty(@NotNull JsonElement jsonElement) {
        jsonElement
            .getAsJsonObject()
            .addProperty(MOD_VERSION_KEY, Fuji.MOD_VERSION);
    }

}
