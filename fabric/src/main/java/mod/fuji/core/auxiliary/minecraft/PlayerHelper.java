package mod.fuji.core.auxiliary.minecraft;

import com.mojang.authlib.GameProfile;
import java.util.function.Consumer;
import mod.fuji.core.config.mapper.wrapper.GameProfileWrapper;
import mod.fuji.core.document.annotation.TestCase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@TestCase(action = "Consider special states of a player.", targets = {
    "A player may be a fake-player from `carpet` mod."
    , "Once a player die, the old ServerPlayerEntity is invalid."
    , "A player may `disconnect` from the server."
    , "A player may in `spectator` game-mode."
    , "If a player is during the transferring of end portal, he is in no dimensions."
})
public class PlayerHelper {

    /**
 * It's possible to generate invalid player name using <code>/player abc++ spawn</code> command.
 **/
    public static String getPlayerName(@NotNull Player player) {
        @NotNull GameProfile gameProfile = player.getGameProfile();
        return AuthlibHelper.getName(gameProfile);
    }

    public static void playSound(@NotNull ServerPlayer player, @NotNull SoundEvent soundEvent, @NotNull SoundSource soundCategory, float volume, float pitch) {
        #if MC_VER <= MC_1_20_4
        player.playSound(soundEvent, soundCategory, volume, pitch);
        #elif MC_VER > MC_1_20_4
        player.playNotifySound(soundEvent, soundCategory, volume, pitch);
        #endif
    }

    public static int getPing(@NotNull ServerPlayer player) {
        #if MC_VER <= MC_1_20_1
        return player.pingMilliseconds;
        #elif MC_VER > MC_1_20_1
        return player.connection.latency();
        #endif
    }

    public static @NotNull ServerLevel getServerWorld(@NotNull ServerPlayer player) {
        #if MC_VER <= MC_1_21_5
        return (ServerLevel) player.level();
        #elif MC_VER > MC_1_21_5 && MC_VER < MC_1_21_9
        return player.level();
        #elif MC_VER >= MC_1_21_9
        return player.level();
        #endif
    }

    public static @NotNull ServerLevel getServerWorld(@NotNull Player player) {
        return getServerWorld((ServerPlayer) player);
    }

    public static PlayerList getPlayerManager() {
        return ServerHelper.getServer().getPlayerList();
    }

    public static void updateDisplayName(@NotNull ServerPlayer player) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);
        PacketHelper.sendPacketToAll(packet);
    }

    public static void updateDisplayNames() {
        Lookup.getOnlinePlayers()
            .forEach(PlayerHelper::updateDisplayName);
    }

    public static void disconnectPlayer(@NotNull ServerPlayer target, @NotNull Component reasonText) {
        target.connection.disconnect(reasonText);
    }

    public static void dismountRidingEntity(@NotNull ServerPlayer player) {
        player.setShiftKeyDown(true);
        player.rideTick();
    }

    public static class Loader {

        private static final String DIMENSION_NBT_KEY = "Dimension";

        private static ServerPlayer makePlayer(@NotNull GameProfile gameProfile) {
            MinecraftServer server = ServerHelper.getServer();

            #if MC_VER <= MC_1_20_1
            return new ServerPlayerEntity(server, server.getOverworld(), gameProfile);
            #elif MC_VER > MC_1_20_1
            var syncedClientOptions = net.minecraft.server.level.ClientInformation.createDefault();
            return new ServerPlayer(server, server.overworld(), gameProfile, syncedClientOptions);
            #endif
        }

        private static void applyPlayerData(
            #if MC_VER < MC_1_21_6
            @NotNull ServerPlayer player, @Nullable net.minecraft.nbt.CompoundTag playerData
            #elif MC_VER >= MC_1_21_6 && MC_VER < MC_1_21_9
            @NotNull ServerPlayer player, @Nullable net.minecraft.world.level.storage.ValueInput playerData
            #elif MC_VER >= MC_1_21_9
            @NotNull ServerPlayer player, @Nullable net.minecraft.nbt.CompoundTag playerData
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
            #elif MC_VER >= MC_1_21_6 && MC_VER < MC_1_21_9
            playerData
                .getString(DIMENSION_NBT_KEY)
                .ifPresent(dimensionId -> setServerWorld(player, dimensionId));
            #elif MC_VER >= MC_1_21_9
            NbtHelper.Primitives
                .getString(playerData, DIMENSION_NBT_KEY)
                .ifPresent(dimensionId -> setServerWorld(player, dimensionId));
            #endif

        }

        private static void setServerWorld(@NotNull ServerPlayer player, @Nullable String dimensionId) {
            Optional<ServerLevel> world = WorldHelper.getWorld(dimensionId);
            world.ifPresent(player::setServerLevel);
        }

        public static ServerPlayer loadDummyPlayer(@NotNull String playerName) {
            /* Check if the target player is online. */
            Optional<ServerPlayer> player = Lookup.getOnlinePlayerByName(playerName);
            if (player.isPresent()) {
                return player.get();
            }

            /* Load game profile. */
            Optional<GameProfile> gameProfile = Cache.getOfflineGameProfileByName(playerName);
            if (gameProfile.isEmpty()) {
                throw new IllegalArgumentException("Can't find player %s in usercache.json file".formatted(playerName));
            }

            /* Make the player instance. */
            ServerPlayer $player = makePlayer(gameProfile.get());

            #if MC_VER <= MC_1_20_4
            net.minecraft.nbt.NbtCompound playerData = getPlayerManager().loadPlayerData($player);
            applyPlayerData($player, playerData);
            #elif MC_VER > MC_1_20_4 && MC_VER < MC_1_21_6
            Optional<net.minecraft.nbt.CompoundTag> playerData = getPlayerManager().load($player);
            applyPlayerData($player, playerData.orElse(null));
            #elif MC_VER >= MC_1_21_6 && MC_VER < MC_1_21_9
            Optional<net.minecraft.world.level.storage.ValueInput> playerData = getPlayerManager().load($player, net.minecraft.util.ProblemReporter.DISCARDING);
            applyPlayerData($player, playerData.orElse(null));
            #elif MC_VER >= MC_1_21_9
            Optional<CompoundTag> playerData = getPlayerManager().loadPlayerData($player.nameAndId());
            applyPlayerData($player, playerData.orElse(null));
            #endif

            return $player;
        }

    }

    public static class Lookup {

        public static List<ServerPlayer> getOnlinePlayers() {
            return getPlayerManager().getPlayers();
        }

        public static List<String> getOnlinePlayerNames() {
            return getOnlinePlayers()
                .stream()
                .map(PlayerHelper::getPlayerName)
                .toList();
        }

        public static Optional<ServerPlayer> getOnlinePlayerByName(@NotNull String playerName) {
            return getOnlinePlayers()
                .stream()
                .filter(it -> getPlayerName(it).equals(playerName))
                .findFirst();
        }

        public static Optional<ServerPlayer> getOnlinePlayerByNameIgnoreCase(@NotNull String playerName) {
            return getOnlinePlayers()
                .stream()
                .filter(it -> getPlayerName(it).equalsIgnoreCase(playerName))
                .findFirst();
        }

        public static Optional<ServerPlayer> getOnlinePlayerByUuid(@NotNull UUID playerUUID) {
            return getOnlinePlayers()
                .stream()
                .filter(player -> player.getUUID().equals(playerUUID))
                .findFirst();
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public static boolean isPlayerOnline(@NotNull String playerName) {
            return getOnlinePlayerByName(playerName)
                .isPresent();
        }
    }

    public static class Kind {

        /**
 * The carpet mod sub-classing the ServerPlayerEntity.
 **/
        public static boolean isRealPlayer(@NotNull ServerPlayer player) {
            return player.getClass() == ServerPlayer.class;
        }

        /**
 *             If a method is called both from client and client integrated server.
            Then it will be called twice, one for ClientPlayerEntity, one for ServerPlayerEntity.
            This happens when you install this mod in the client side, and plays in the single-player world.

 **/
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public static boolean isServerPlayer(@NotNull Player player) {
            return player instanceof ServerPlayer;
        }


        /**
 *             If your mod is installed on the client-side, and run the single-player world.
            Then some functions will be called twice.
            One for ClientPlayerEntity, one for ServerPlayerEntity.

 **/
        public static void withServerPlayerEntity(@Nullable Player player, @NotNull Consumer<ServerPlayer> consumer) {
            if (player == null) return;
            if (!isServerPlayer(player)) return;
            consumer.accept((ServerPlayer) player);
        }

        public static void withServerPlayerEntity(@Nullable Player player, @NotNull Runnable runnable) {
            withServerPlayerEntity(player, (serverPlayerEntity) -> runnable.run());
        }
    }

    public static class Cache {

        public static @NotNull List<GameProfile> getOfflineGameProfiles() {
            /* Get the user cache. */
            return getUserCache()
                .map($userCache -> {
                    /* Make the list from user cache. */
                    return $userCache.profilesByName.values()
                        .stream()
                        .map(GameProfileWrapper::fromVanillaType)
                        .map(GameProfileWrapper::toGameProfile)
                        // Filter out invalid game profile cache entry.
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
                })
                .orElseGet(List::of);
        }

        public static
        #if MC_VER <= MC_1_21_6
        Optional<net.minecraft.server.players.GameProfileCache>
        #elif MC_VER > MC_1_21_6
        Optional<net.minecraft.server.players.CachedUserNameToIdResolver>
        #endif
        getUserCache() {
            #if MC_VER < MC_1_21_9
            return Optional.ofNullable(ServerHelper.getServer().getProfileCache());
            #elif MC_VER >= MC_1_21_9
            //noinspection OptionalOfNullableMisuse
            return Optional.ofNullable((net.minecraft.server.players.CachedUserNameToIdResolver) ServerHelper.getServer().services().nameToIdCache());
            #endif
        }

        public static @NotNull List<String> getOfflinePlayerNames() {
            return getOfflineGameProfiles()
                .stream()
                .map(AuthlibHelper::getName)
                .toList();
        }

        public static Optional<GameProfile> getOfflineGameProfileByName(@NotNull String playerName) {
            // NOTE: Only find the game profile from existing cache, don't compute the value from Mojang server.
            return getOfflineGameProfiles()
                .stream()
                .filter(it -> AuthlibHelper.getName(it).equals(playerName))
                .findFirst();
        }
    }

}
