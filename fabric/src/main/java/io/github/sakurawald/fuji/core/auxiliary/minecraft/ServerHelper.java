package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

@TestCase(steps = "Consider the possible runtime environments.", purposes = {
    "The fabric server-side environment."
    , "The fabric client-side environment."
    , "The neo-forge server-side environment. (With `sinytra-connector` mod)"
    , "The neo-forge client-side environment. (With `sinytra-connector` mod)"
    , "The hybrid server (forge+bukkit) with `sinytra-connector` mod"
    , "The GraalVM native image. (Which invalidates the reflection)"
})
public class ServerHelper {

    @Getter
    @Setter
    private static MinecraftServer server;

    public static void executeSync(Runnable runnable) {
        getServer().executeSync(runnable);
    }

    @ForDeveloper("""
    If a method is called both in client-side and server-side. Then it will be called twice if the mod is installed in the client-side.
    One for the client, one for the client integrated server.
    """)
    public static boolean isClientSideIntegratedServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isServerSideDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

}
