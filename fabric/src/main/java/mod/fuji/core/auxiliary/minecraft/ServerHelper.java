package mod.fuji.core.auxiliary.minecraft;

import mod.fuji.Fuji;
import mod.fuji.core.document.annotation.TestCase;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

@TestCase(action = "Consider the possible runtime environments.", targets = {
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
        getServer().executeIfPossible(runnable);
    }

    public static class Lifecycle {

        /**
 * Returns if the server instance is instantiated.
 **/
        public static boolean isServerInstantiated() {
            return server != null;
        }

        public static void ifServerInstantiated(Runnable runnable) {
            if (isServerInstantiated()) {
                runnable.run();
            }
        }
    }

    public static class Environment {

        public static boolean isClientSideIntegratedServer() {
            return getPhysicalEnvironmentType() == EnvType.CLIENT;
        }

        public static boolean isServerSideDedicatedServer() {
            return getPhysicalEnvironmentType() == EnvType.SERVER;
        }

        /**
 *             If a method is called both in client-side and server-side. Then it will be called twice if the mod is installed in the client-side.
            One for the client, one for the client integrated server.

 **/
        public static @NotNull EnvType getPhysicalEnvironmentType() {
            return FabricLoader.getInstance().getEnvironmentType();
        }

        public static void withDevelopmentEnvironment(@NotNull Runnable runnable) {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                runnable.run();
            }
        }
    }

    public static class ModInfo {

        public static @NotNull ModContainer getSelfModContainer() {
            return FabricLoader.getInstance()
                .getModContainer(Fuji.MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Failed to get 'fuji' mod container."));
        }
    }

}
