package mod.fuji;

import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.manager.Managers;
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

    @Override
    public void onInitialize() {
        Managers.getPrimaryBackupManager().onInitialize();
        Managers.getEventManager().onInitialize();
        Managers.getBossBarManager().onInitialize();
        Managers.getGameTaskManager().onInitialize();
        Managers.getCacheManager().onInitialize();
        Managers.getCallbackManager().onInitialize();
        Managers.getModuleManager().onInitialize();
        Managers.getScheduleManager().onInitialize();
    }
}
