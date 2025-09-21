package mod.fuji.core.config.migrator.version;

import com.google.gson.JsonElement;
import mod.fuji.Fuji;
import mod.fuji.core.document.annotation.ForDeveloper;
import org.jetbrains.annotations.NotNull;

public class VersionPropertyInjector {

    public static final String MOD_VERSION_KEY = "MOD_VERSION";

    @ForDeveloper("The mod version string in only available since v12.13.0")
    public static final String FUTURE_MOD_VERSION = "999.999.999";

    public static final String LEGACY_MOD_VERSION = "1.0.0";


    public static void injectVersionProperty(@NotNull JsonElement jsonElement) {
        jsonElement
            .getAsJsonObject()
            .addProperty(MOD_VERSION_KEY, Fuji.MOD_VERSION);
    }

}
