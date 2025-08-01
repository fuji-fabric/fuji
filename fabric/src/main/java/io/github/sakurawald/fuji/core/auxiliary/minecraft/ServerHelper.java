package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;

#if  MC_VER <= MC_1_20_6
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
#elif MC_VER > MC_1_20_6
import net.minecraft.server.world.ServerChunkLoadingManager;
#endif

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public static Collection<ServerWorld> getWorlds() {
        return getServer()
            .worlds
            .values();
    }

    public static Optional<ServerWorld> getWorld(String dimensionId) {
        return getWorlds()
            .stream()
            .filter(it -> RegistryHelper.toString(it).equals(dimensionId))
            .findFirst();
    }

    public static @NotNull ServerWorld getWorldOrThrow(String dimensionId) {
        return getServerWorld(dimensionId)
            .orElseThrow(() -> new IllegalStateException("Dimension %s not found.".formatted(dimensionId)));
    }

    public static @Nullable CommandDispatcher<ServerCommandSource> getCommandDispatcher() {
        // NOTE: It's null on server startup.
        if (getServer() == null
            || getServer().getCommandManager() == null) {
            return null;
        }

        return getServer().getCommandManager().getDispatcher();
    }

    public static PlayerManager getPlayerManager() {
        return getServer().getPlayerManager();
    }

    public static List<ServerPlayerEntity> getOnlinePlayers() {
        return getPlayerManager().getPlayerList();
    }

    public static List<String> getOnlinePlayerNames() {
        return getOnlinePlayers()
            .stream()
            .map(PlayerHelper::getPlayerName)
            .toList();
    }

    public static void executeSync(Runnable runnable) {
        getServer().executeSync(runnable);
    }

    public static Optional<ServerPlayerEntity> getOnlinePlayerByName(String name) {
        return getOnlinePlayers()
            .stream()
            .filter(it -> PlayerHelper.getPlayerName(it).equals(name))
            .findFirst();
    }

    public static Optional<ServerPlayerEntity> getOnlinePlayerByNameIgnoreCase(String name) {
        return getOnlinePlayers()
            .stream()
            .filter(it -> PlayerHelper.getPlayerName(it).equalsIgnoreCase(name))
            .findFirst();
    }

    public static Optional<ServerPlayerEntity> getOnlinePlayerByUuid(UUID uuid) {
        return getOnlinePlayers()
            .stream()
            .filter(player -> player.getUuid().equals(uuid))
            .findFirst();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPlayerOnline(String playerName) {
        return getOnlinePlayerByName(playerName).isPresent();
    }

    public static void sendPacketToAll(Packet<?> packet) {
        getPlayerManager()
            .sendToAll(packet);
    }

    @SuppressWarnings("unused")
    public static void sendPacket(Packet<?> packet, ServerPlayerEntity player) {
        player.networkHandler.sendPacket(packet);
    }

    @SuppressWarnings("unused")
    public static void sendPacketToAllExcept(Packet<?> packet, ServerPlayerEntity player) {
        getPlayerManager()
            .getPlayerList()
            .stream()
            .filter(it -> !it.equals(player))
            .forEach(p -> sendPacket(packet, player));
    }

    public static void updateDisplayName() {
        getOnlinePlayers()
            .forEach(player -> {
                PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
                sendPacketToAll(packet);
            });
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

    public static List<GameProfile> getOfflineGameProfiles() {
        UserCache userCache = ServerHelper.getServer().getUserCache();
        if (userCache == null) return List.of();

        return userCache.byName.values()
            .stream()
            .map(UserCache.Entry::getProfile)
            .toList();
    }

    public static Optional<GameProfile> getOfflineGameProfileByName(String playerName) {
        UserCache userCache = ServerHelper.getServer().getUserCache();
        if (userCache == null) return Optional.empty();

        UserCache.Entry entry = userCache.byName.get(playerName);
        if (entry == null || entry.getProfile() == null) {
            return Optional.empty();
        }
        return Optional.of(entry.getProfile());
    }


    public static @NotNull List<String> getOfflinePlayerNames() {
        /* Get the user cache. */
        UserCache userCache = getServer().getUserCache();
        if (userCache == null) return List.of();

        /* Make the list from user cache. */
        return userCache.byName
            .values()
            .stream()
            .map(it -> it.getProfile().getName())
            .toList();
    }

    public static boolean isClientSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isServerSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    public static Optional<ServerWorld> getServerWorld(@Nullable String identifier) {
        if (identifier == null) return Optional.empty();

        RegistryKey<World> key = RegistryHelper.ofRegistryKey(RegistryKeys.WORLD, RegistryHelper.makeIdentifierOrThrow(identifier));
        return Optional.ofNullable(getServer()
            .getWorld(key));
    }
}
