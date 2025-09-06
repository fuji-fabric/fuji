package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        getServer().executeSync(runnable);
    }

    public static boolean isServerInstantiated() {
        return server != null;
    }

    public static void withServerInstantiated(Runnable runnable) {
        if (isServerInstantiated()) {
            runnable.run();
        }
    }

    @ForDeveloper("""
        If a method is called both in client-side and server-side. Then it will be called twice if the mod is installed in the client-side.
        One for the client, one for the client integrated server.
        """)
    public static boolean isClientSideIntegratedServer() {
        return getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isServerSideDedicatedServer() {
        return getEnvironmentType() == EnvType.SERVER;
    }

    public static @NotNull EnvType getEnvironmentType() {
        return FabricLoader.getInstance().getEnvironmentType();
    }

    @ForDeveloper("""
        If your mod is installed on the client-side, and run the single-player world.
        Then some functions will be called twice.
        One for ClientPlayerEntity, one for ServerPlayerEntity.
        """)
    public static void withServerPlayerEntity(@Nullable PlayerEntity player, @NotNull Runnable runnable) {
        if (player == null) return;
        if (!PlayerHelper.isServerPlayer(player)) return;
        runnable.run();
    }

    public static @NotNull ModContainer getSelfModContainer() {
        return FabricLoader.getInstance()
            .getModContainer(Fuji.MOD_ID)
            .orElseThrow(() -> new IllegalStateException("Failed to get 'fuji' mod container."));
    }

    public static void withDevelopmentEnvironment(@NotNull Runnable runnable) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            runnable.run();
        }
    }

}
