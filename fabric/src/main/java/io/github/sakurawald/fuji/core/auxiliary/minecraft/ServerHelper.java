package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkHolder;

#if  MC_VER <= MC_1_20_6
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
#elif MC_VER > MC_1_20_6
import net.minecraft.server.world.ServerChunkLoadingManager;
#endif

import net.minecraft.server.world.ServerWorld;

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

    public static
    #if  MC_VER <= MC_1_20_6
    ThreadedAnvilChunkStorage
    #elif MC_VER > MC_1_20_6
    ServerChunkLoadingManager
    #endif
    getChunkStorage(ServerWorld world) {
        #if MC_VER <= MC_1_20_6
        return world.getChunkManager().threadedAnvilChunkStorage;
        #elif MC_VER > MC_1_20_6
        return world.getChunkManager().chunkLoadingManager;
        #endif
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static Iterable<ChunkHolder> getChunks(ServerWorld world) {
        Iterable<ChunkHolder> chunkHolders = getChunkStorage(world).entryIterator();
        return chunkHolders;
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
