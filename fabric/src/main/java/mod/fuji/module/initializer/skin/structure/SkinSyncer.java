package mod.fuji.module.initializer.skin.structure;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.document.annotation.TestCase;
import java.util.Collections;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;

#if MC_VER <= MC_1_20_1
import net.minecraft.world.biome.source.BiomeAccess;
#endif

public class SkinSyncer {

    public static void broadcastGameProfileChange(@NotNull ServerPlayer player) {
        PlayerHelper.Lookup.getOnlinePlayers()
            .forEach(observer -> {
                sendPacketsToOnlinePlayers(observer, player);

                if (observer.equals(player)) {
                    sendPacketsToSelfPlayer(player);
                } else {
                    sendPacketsToObservingPlayers(observer, player);
                }
            });
    }

    @TestCase(action = "Try to send a chat message after the skin changed in online-mode server.", targets = {
        "The chat message validation should be proper after a player changed its skin."
        , "It should work in both `online-mode` and `offline-mode` servers."
    })
    private static void sendPacketsToSelfPlayer(@NotNull ServerPlayer player) {
        // NOTE: This function is used to simulate the PlayerManager#respawnPlayer
        if (player.isRemoved()) {
            LogUtil.debug("Skip the sending packets to the self player, because it's already removed. (player = {})", PlayerHelper.getPlayerName(player));
            return;
        }

        /* Send re-spawn packet to the player, to simulate the dimension change behaviour. */
        #if MC_VER <= MC_1_20_1
        player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.getWorld().getDimensionKey(), player.getWorld().getRegistryKey(), BiomeAccess.hashSeed(player.getServerWorld().getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), player.getWorld().isDebugWorld(), player.getServerWorld().isFlat(), (byte) 3, player.getLastDeathPos(), player.getPortalCooldown()));
        #elif MC_VER > MC_1_20_1
        player.connection.send(new ClientboundRespawnPacket(player.createCommonSpawnInfo(EntityHelper.getServerWorld(player)), (byte) 3));
        #endif

        /* Re-initialize the chat to prevent chat validation error in the client. */
        player.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT, player));

        /* Update the position and rotation. (Does not harm) */
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

        /* Restore the velocity of the target player. */
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        /* Restore the previous difficulty. */
        player.connection.send(new ClientboundChangeDifficultyPacket(EntityHelper.getServerWorld(player).getDifficulty(), EntityHelper.getServerWorld(player).getLevelData().isDifficultyLocked()));

        /* Restore the previous inventory slots + selected slot. */
        PlayerHelper.getPlayerManager().sendAllPlayerInfo(player);

        /* Restore the previous abilities. */
        player.onUpdateAbilities();

        /* Restore the previous status effects. */
        for (MobEffectInstance effect : player.getActiveEffects()) {
        #if MC_VER <= MC_1_20_4
        player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect));
        #elif MC_VER > MC_1_20_4
        player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect, false));
        #endif
        }

        /* Restore the previous experience. */
        player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));

        /* Restore the previous vehicle and passengers of the player. */
        syncVehicleAndPassengers(player, player);

        /* Send the dimension info to the client to prevent it from getting stuck on the dimension loading screen. */
        PlayerHelper.getPlayerManager().sendLevelInfo(player, PlayerHelper.getServerWorld(player));

        /* Update the entity tracker. (Does not harm) */
        player.connection.send(new ClientboundSetEntityDataPacket(player.getId(), player.getEntityData().getNonDefaultValues()));
    }

    private static void syncVehicleAndPassengers(@NotNull ServerPlayer observer, @NotNull ServerPlayer player) {
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            observer.connection.send(new ClientboundSetPassengersPacket(vehicle));
        }
        observer.connection.send(new ClientboundSetPassengersPacket(player));
    }

    private static void sendPacketsToObservingPlayers(@NotNull ServerPlayer observer, @NotNull ServerPlayer player) {
        /* Re-bind the observer.networkHandler to the player entity. */
        ServerLevel playerServerWorld = PlayerHelper.getServerWorld(player);
        var trackedPlayer = WorldHelper.getChunkStorage(playerServerWorld).entityMap.get(player.getId());
        if (trackedPlayer != null) {
            trackedPlayer.removePlayer(observer);
            trackedPlayer.updatePlayer(observer);
        }

        /* Sync the passengers. */
        observer.connection.send(new ClientboundSetPassengersPacket(player));
    }

    private static void sendPacketsToOnlinePlayers(@NotNull ServerPlayer observer, @NotNull ServerPlayer player) {
        /* Update the game profile in player list. */
        observer.connection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(player.getUUID())));
        observer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, player));
        observer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, player));
        observer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
    }
}
