package io.github.sakurawald.core.auxiliary.minecraft;

import com.mojang.authlib.GameProfile;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
#if MC_VER >= MC_1_21_6
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
#endif
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@UtilityClass
public class PlayerHelper {

    private static final String DIMENSION_NBT_KEY = "Dimension";

    public static ServerPlayerEntity makePlayer(GameProfile gameProfile) {
        MinecraftServer server = ServerHelper.getServer();
        SyncedClientOptions syncedClientOptions = SyncedClientOptions.createDefault();
        return new ServerPlayerEntity(server, server.getOverworld(), gameProfile, syncedClientOptions);
    }

    private static void applyPlayerData(ServerPlayerEntity player, @Nullable NbtCompound playerData) {
        if (playerData == null) return;

        /* Apply saved dimension */
        if (playerData.contains(DIMENSION_NBT_KEY)) {
            String dimensionId = NbtHelper.getString(playerData, DIMENSION_NBT_KEY);
            setServerWorld(player, dimensionId);
        }
    }

    #if MC_VER >= MC_1_21_6
    private static void applyPlayerData(ServerPlayerEntity player, @Nullable ReadView playerData) {
         playerData.getOptionalString(DIMENSION_NBT_KEY)
            .ifPresent(dimensionId -> setServerWorld(player, dimensionId));
    }
    #endif

    public static boolean isRealPlayer(@NotNull ServerPlayerEntity player) {
        return player.getClass() == ServerPlayerEntity.class;
    }

    public static ServerPlayerEntity loadOfflinePlayer(String playerName) {
        Optional<GameProfile> gameProfile = getGameProfileByName(playerName);
        if (gameProfile.isEmpty()) {
            throw new IllegalArgumentException("can't find player %s in usercache.json".formatted(playerName));
        }

        ServerPlayerEntity player = makePlayer(gameProfile.get());

            /*
             the default dimension for ServerPlayerEntity instance is minecraft:overworld.
             in order to keep original dimension, here we should set dimension for the loaded player entity.
             */
        #if MC_VER < MC_1_21_6
        Optional<NbtCompound> playerDataOpt = ServerHelper.getPlayerManager().loadPlayerData(player);
        applyPlayerData(player, playerDataOpt.orElse(null));
        #elif MC_VER >= MC_1_21_6
        Optional<ReadView> playerDataOpt = ServerHelper.getPlayerManager().loadPlayerData(player, ErrorReporter.Impl.EMPTY);
        applyPlayerData(player,playerDataOpt.get());
        #endif

        return player;
    }

    private static Optional<GameProfile> getGameProfileByName(String playerName) {
        UserCache userCache = ServerHelper.getServer().getUserCache();
        if (userCache == null) return Optional.empty();

        return userCache.findByName(playerName);
    }

    public static void setServerWorld(ServerPlayerEntity player, String dimensionId) {
        ServerWorld world = RegistryHelper.ofServerWorld(dimensionId);
        if (world != null) {
            player.setServerWorld(world);
        }
    }

}
