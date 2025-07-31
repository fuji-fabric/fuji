package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
#if MC_VER <= MC_1_20_1
#elif MC_VER > MC_1_20_1
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
#endif
import net.minecraft.server.MinecraftServer;
#if MC_VER <= MC_1_20_1
#elif MC_VER > MC_1_20_1
#endif

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
#if MC_VER >= MC_1_21_6
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
#endif
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@TestCase(steps = "Consider special states of a player.", purposes = {
    "A player may be a fake-player from `carpet` mod."
    , "Once a player die, the old ServerPlayerEntity is invalid."
    , "A player may `disconnect` from the server."
    , "A player may in `spectator` game-mode."
    , "If a player is during the transferring of end portal, he is in no dimensions."
})
public class PlayerHelper {

    private static final String DIMENSION_NBT_KEY = "Dimension";

    private static ServerPlayerEntity makePlayer(@NotNull GameProfile gameProfile) {
        MinecraftServer server = ServerHelper.getServer();

        #if MC_VER <= MC_1_20_1
        return new ServerPlayerEntity(server, server.getOverworld(), gameProfile);
        #elif MC_VER > MC_1_20_1
        SyncedClientOptions syncedClientOptions = SyncedClientOptions.createDefault();
        return new ServerPlayerEntity(server, server.getOverworld(), gameProfile, syncedClientOptions);
        #endif
    }

    private static void applyPlayerData(@NotNull ServerPlayerEntity player, @Nullable NbtCompound playerData) {
        if (playerData == null) return;

        /* Apply saved dimension. */
        if (playerData.contains(DIMENSION_NBT_KEY)) {
            String dimensionId = NbtHelper.Primitives.getString(playerData, DIMENSION_NBT_KEY);
            setServerWorld(player, dimensionId);
        }
    }

    #if MC_VER >= MC_1_21_6
    private static void applyPlayerData(ServerPlayerEntity player, @Nullable ReadView playerData) {
         playerData.getOptionalString(DIMENSION_NBT_KEY)
            .ifPresent(dimensionId -> setServerWorld(player, dimensionId));
    }
    #endif

    public static ServerPlayerEntity loadServerPlayerEntity(String playerName) {
        /* Check if the target player is online. */
        Optional<ServerPlayerEntity> onlinePlayerByName = ServerHelper.getOnlinePlayerByName(playerName);
        if (onlinePlayerByName.isPresent()) {
            return onlinePlayerByName.get();
        }

        /* Load game profile. */
        Optional<GameProfile> gameProfile = getGameProfileByName(playerName);
        if (gameProfile.isEmpty()) {
            throw new IllegalArgumentException("Can't find player %s in usercache.json".formatted(playerName));
        }

        /* Make the player instance. */
        ServerPlayerEntity player = makePlayer(gameProfile.get());

        #if MC_VER <= MC_1_20_4
        NbtCompound playerDataOpt = ServerHelper.getPlayerManager().loadPlayerData(player);
        applyPlayerData(player, playerDataOpt);
        #elif MC_VER > MC_1_20_4 && MC_VER < MC_1_21_6
        Optional<NbtCompound> playerDataOpt = ServerHelper.getPlayerManager().loadPlayerData(player);
        applyPlayerData(player, playerDataOpt.orElse(null));
        #elif MC_VER >= MC_1_21_6
        Optional<ReadView> playerDataOpt = ServerHelper.getPlayerManager().loadPlayerData(player, ErrorReporter.Impl.EMPTY);
        applyPlayerData(player, playerDataOpt.get());
        #endif

        return player;
    }

    public static Optional<GameProfile> getGameProfileByName(String playerName) {
        // NOTE: Get the game profile used by this server.
        UserCache userCache = ServerHelper.getServer().getUserCache();
        if (userCache == null) return Optional.empty();

        return userCache.findByName(playerName);
    }

    public static void setServerWorld(@NotNull ServerPlayerEntity player, @Nullable String dimensionId) {
        ServerWorld world = RegistryHelper.getServerWorld(dimensionId);
        if (world != null) {
            player.setServerWorld(world);
        }
    }

    public static void playSound(ServerPlayerEntity player, SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
        #if MC_VER <= MC_1_20_4
        player.playSound(soundEvent, soundCategory, volume, pitch);
        #elif MC_VER > MC_1_20_4
        player.playSoundToPlayer(soundEvent, soundCategory, volume, pitch);
        #endif
    }

    public static int getPing(ServerPlayerEntity player) {
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

    public static String getPlayerName(PlayerEntity player) {
        return player.getGameProfile().getName();
    }

    public static boolean isRealPlayer(@NotNull ServerPlayerEntity player) {
        // NOTE: The carpet mod subclassing the ServerPlayerEntity.
        return player.getClass() == ServerPlayerEntity.class;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isServerPlayer(PlayerEntity player) {
        return player instanceof ServerPlayerEntity;
    }

    public static boolean isOp(PlayerEntity player) {
        return ServerHelper
            .getServer()
            .getPlayerManager()
            .isOperator(player.getGameProfile());
    }

    public static boolean isAdmin(ServerCommandSource source) {
        return source.hasPermissionLevel(4);
    }

    public static ServerWorld getServerWorld(ServerPlayerEntity player) {
        #if MC_VER <= MC_1_21_5
        return (ServerWorld) player.getWorld();
        #elif MC_VER > MC_1_21_5
        return player.getWorld();
        #endif
    }
}
