package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@TestCase(steps = "Consider special states of a player.", purposes = {
    "A player may be a fake-player from `carpet` mod."
    , "Once a player die, the old ServerPlayerEntity is invalid."
    , "A player may `disconnect` from the server."
    , "A player may in `spectator` game-mode."
    , "If a player is during the transferring of end portal, he is in no dimensions."
})
public class PlayerHelper {

    public static String getPlayerName(@NotNull PlayerEntity player) {
        return player.getGameProfile().getName();
    }

    public static void playSound(@NotNull ServerPlayerEntity player, @NotNull SoundEvent soundEvent, @NotNull SoundCategory soundCategory, float volume, float pitch) {
        #if MC_VER <= MC_1_20_4
        player.playSound(soundEvent, soundCategory, volume, pitch);
        #elif MC_VER > MC_1_20_4
        player.playSoundToPlayer(soundEvent, soundCategory, volume, pitch);
        #endif
    }

    public static int getPing(@NotNull ServerPlayerEntity player) {
        #if MC_VER <= MC_1_20_1
        return player.pingMilliseconds;
        #elif MC_VER > MC_1_20_1
        return player.networkHandler.getLatency();
        #endif
    }

    public static String getPropertyValue(@NotNull Property property) {
        #if MC_VER <= MC_1_20_1
        return property.getValue();
        #elif MC_VER > MC_1_20_1
        return property.value();
        #endif
    }

    @ForDeveloper("The carpet mod sub-classing the ServerPlayerEntity.")
    public static boolean isRealPlayer(@NotNull ServerPlayerEntity player) {
        return player.getClass() == ServerPlayerEntity.class;
    }

    @ForDeveloper("""
        If a method is called both from client and client integrated server.
        Then it will be called twice, one for ClientPlayerEntity, one for ServerPlayerEntity.
        This happens when you install this mod in the client side, and plays in the single-player world.
        """)
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isServerPlayer(@NotNull PlayerEntity player) {
        return player instanceof ServerPlayerEntity;
    }

    public static boolean isOperator(@NotNull PlayerEntity player) {
        return ServerHelper
            .getServer()
            .getPlayerManager()
            .isOperator(player.getGameProfile());
    }

    public static boolean isAdmin(@NotNull ServerCommandSource source) {
        return source.hasPermissionLevel(4);
    }

    public static @NotNull ServerWorld getServerWorld(@NotNull ServerPlayerEntity player) {
        #if MC_VER <= MC_1_21_5
        return (ServerWorld) player.getWorld();
        #elif MC_VER > MC_1_21_5
        return player.getWorld();
        #endif
    }

    public static PlayerManager getPlayerManager() {
        return ServerHelper.getServer().getPlayerManager();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPlayerOnline(@NotNull String playerName) {
        return Lookup.getOnlinePlayerByName(playerName)
            .isPresent();
    }

    public static void updateDisplayName(@NotNull ServerPlayerEntity player) {
        PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
        PacketHelper.sendPacketToAll(packet);
    }

    public static void updateDisplayNames() {
        Lookup.getOnlinePlayers()
            .forEach(PlayerHelper::updateDisplayName);
    }

    public static class Loader {

        private static final String DIMENSION_NBT_KEY = "Dimension";

        private static ServerPlayerEntity makePlayer(@NotNull GameProfile gameProfile) {
            MinecraftServer server = ServerHelper.getServer();

            #if MC_VER <= MC_1_20_1
            return new ServerPlayerEntity(server, server.getOverworld(), gameProfile);
            #elif MC_VER > MC_1_20_1
            var syncedClientOptions = net.minecraft.network.packet.c2s.common.SyncedClientOptions.createDefault();
            return new ServerPlayerEntity(server, server.getOverworld(), gameProfile, syncedClientOptions);
            #endif
        }

        private static void applyPlayerData(
            #if MC_VER < MC_1_21_6
            @NotNull ServerPlayerEntity player, @Nullable NbtCompound playerData
            #elif MC_VER >= MC_1_21_6
            @NotNull ServerPlayerEntity player, @Nullable net.minecraft.storage.ReadView playerData
            #endif
        ) {
            /* Do nothing if player data is null. */
            if (playerData == null) return;

            /* Restore previous dimension. */
            #if MC_VER < MC_1_21_6
            if (playerData.contains(DIMENSION_NBT_KEY)) {
                String dimensionId = NbtHelper.Primitives.getString(playerData, DIMENSION_NBT_KEY).get();
                setServerWorld(player, dimensionId);
            }
            #elif MC_VER >= MC_1_21_6
            playerData
                .getOptionalString(DIMENSION_NBT_KEY)
                .ifPresent(dimensionId -> setServerWorld(player, dimensionId));
            #endif
        }

        private static void setServerWorld(@NotNull ServerPlayerEntity player, @Nullable String dimensionId) {
            Optional<ServerWorld> world = WorldHelper.getWorld(dimensionId);
            world.ifPresent(player::setServerWorld);
        }

        public static ServerPlayerEntity loadDummyPlayer(@NotNull String playerName) {
            /* Check if the target player is online. */
            Optional<ServerPlayerEntity> player = Lookup.getOnlinePlayerByName(playerName);
            if (player.isPresent()) {
                return player.get();
            }

            /* Load game profile. */
            Optional<GameProfile> gameProfile = Cache.getOfflineGameProfileByName(playerName);
            if (gameProfile.isEmpty()) {
                throw new IllegalArgumentException("Can't find player %s in usercache.json file".formatted(playerName));
            }

            /* Make the player instance. */
            ServerPlayerEntity $player = makePlayer(gameProfile.get());

            #if MC_VER <= MC_1_20_4
            NbtCompound playerData = getPlayerManager().loadPlayerData($player);
            applyPlayerData($player, playerData);
            #elif MC_VER > MC_1_20_4 && MC_VER < MC_1_21_6
            Optional<NbtCompound> playerData = getPlayerManager().loadPlayerData($player);
            applyPlayerData($player, playerData.orElse(null));
            #elif MC_VER >= MC_1_21_6
            Optional<net.minecraft.storage.ReadView> playerData = getPlayerManager().loadPlayerData($player, net.minecraft.util.ErrorReporter.Impl.EMPTY);
            applyPlayerData($player, playerData.orElse(null));
            #endif

            return $player;
        }

    }

    public static class Lookup {

        public static List<ServerPlayerEntity> getOnlinePlayers() {
            return getPlayerManager().getPlayerList();
        }

        public static List<String> getOnlinePlayerNames() {
            return getOnlinePlayers()
                .stream()
                .map(PlayerHelper::getPlayerName)
                .toList();
        }

        public static Optional<ServerPlayerEntity> getOnlinePlayerByName(@NotNull String playerName) {
            return getOnlinePlayers()
                .stream()
                .filter(it -> getPlayerName(it).equals(playerName))
                .findFirst();
        }

        public static Optional<ServerPlayerEntity> getOnlinePlayerByNameIgnoreCase(@NotNull String playerName) {
            return getOnlinePlayers()
                .stream()
                .filter(it -> getPlayerName(it).equalsIgnoreCase(playerName))
                .findFirst();
        }

        public static Optional<ServerPlayerEntity> getOnlinePlayerByUuid(@NotNull UUID playerUUID) {
            return getOnlinePlayers()
                .stream()
                .filter(player -> player.getUuid().equals(playerUUID))
                .findFirst();
        }
    }

    public static class Cache {

        public static List<GameProfile> getOfflineGameProfiles() {
            /* Get the user cache. */
            UserCache userCache = ServerHelper.getServer().getUserCache();
            if (userCache == null) return List.of();

            /* Make the list from user cache. */
            return userCache.byName.values()
                .stream()
                .map(UserCache.Entry::getProfile)
                .toList();
        }

        public static @NotNull List<String> getOfflinePlayerNames() {
            return getOfflineGameProfiles()
                .stream()
                .map(GameProfile::getName)
                .toList();
        }

        public static Optional<GameProfile> getOfflineGameProfileByName(@NotNull String playerName) {
            return getOfflineGameProfiles()
                .stream()
                .filter(it -> it.getName().equals(playerName))
                .findFirst();
        }
    }

}
