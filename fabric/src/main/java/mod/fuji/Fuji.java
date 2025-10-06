package mod.fuji;

import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.lifecycle.ModInitializers;
import mod.fuji.module.initializer.core.CoreInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;


@Cite({
    "https://github.com/EssentialsX/Essentials/"
    , "https://github.com/NucleusPowered/Nucleus"
    , "https://github.com/Zrips/CMI-API"
    , "https://github.com/ForgeEssentials/ForgeEssentials"
})
public class Fuji implements ModInitializer {

    public static final String MOD_ID = "fuji";
    public static final Path MOD_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toAbsolutePath();
    public static final String MOD_VERSION = CoreInitializer.getModVersion();

    /**
     * The <code>onInitialize()</code> method on fabric platform behaves differently depending on context:
     * <ol>
     *     <li>If the mod is installed on server-side, then it will be called after game classes are initialized.</li>
     *     <li>If the mod is installed on client-side, then it will be called after game classes are initialized, but before the <code>integrated server</code> initialization.</li>
     * </ol>
     * The initializer method is called exactly once, during a Minecraft client/server session.
     * For client-side compatibility, use {@link mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent} to obtain a precise and reentrant initialization point.
     */
    @Override
    public void onInitialize() {
        ModInitializers.getPrimaryBackupManager().onInitialize();
        ModInitializers.getEventManager().onInitialize();
        ModInitializers.getModuleManager().onInitialize();
    }
}
