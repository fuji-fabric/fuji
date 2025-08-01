package io.github.sakurawald.fuji.module.initializer.skin.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import java.util.Collections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;

#if MC_VER <= MC_1_20_1
import net.minecraft.world.biome.source.BiomeAccess;
#endif

public class SkinSyncer {

    public static void broadcastGameProfileChange(@NotNull ServerPlayerEntity player) {
        PlayerHelper.getOnlinePlayers()
            .forEach(observer -> {
                sendPacketsToOnlinePlayers(player, observer);

                if (observer.equals(player)) {
                    sendPacketsToSelfPlayer(player);
                } else {
                    sendPacketsToObservingPlayers(player, observer);
                }
            });
    }

    private static void sendPacketsToSelfPlayer(@NotNull ServerPlayerEntity player) {
        // NOTE: This function is used to simulate the PlayerManager#respawnPlayer
        if (player.isRemoved()) {
            LogUtil.debug("Skip the sending packets to the self player, because it's already removed. (player = {})", PlayerHelper.getPlayerName(player));
            return;
        }

        /* Send re-spawn packet to the player, to simulate the dimension change behaviour. */
        #if MC_VER <= MC_1_20_1
        player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.getWorld().getDimensionKey(), player.getWorld().getRegistryKey(), BiomeAccess.hashSeed(player.getServerWorld().getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), player.getWorld().isDebugWorld(), player.getServerWorld().isFlat(), (byte) 3, player.getLastDeathPos(), player.getPortalCooldown()));
        #elif MC_VER > MC_1_20_1
        player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(EntityHelper.getServerWorld(player)), (byte) 3));
        #endif

        /* Update the position and rotation. (Does not harm) */
        player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());

        /* Restore the velocity of the target player. */
        player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));

        /* Restore the previous difficulty. */
        player.networkHandler.sendPacket(new DifficultyS2CPacket(EntityHelper.getServerWorld(player).getDifficulty(), EntityHelper.getServerWorld(player).getLevelProperties().isDifficultyLocked()));

        /* Restore the previous inventory slots + selected slot. */
        PlayerHelper.getPlayerManager().sendPlayerStatus(player);

        /* Restore the previous abilities. */
        player.sendAbilitiesUpdate();

        /* Restore the previous status effects. */
        for (StatusEffectInstance effect : player.getStatusEffects()) {
        #if MC_VER <= MC_1_20_4
        player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), effect));
        #elif MC_VER > MC_1_20_4
        player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), effect, false));
        #endif
        }

        /* Restore the previous experience. */
        player.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));

        /* Restore the previous vehicle and passengers of the player. */
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
        }
        player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));

        /* Send the dimension info to the client to prevent it from getting stuck on the dimension loading screen. */
        PlayerHelper.getPlayerManager().sendWorldInfo(player, PlayerHelper.getServerWorld(player));

        /* Update the entity tracker. (Does not harm) */
        player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
    }

    private static void sendPacketsToObservingPlayers(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity observer) {
        /* Re-bind the observer.networkHandler to the player entity. */
        ServerWorld playerServerWorld = PlayerHelper.getServerWorld(player);
        var trackedPlayer = WorldHelper.getChunkStorage(playerServerWorld).entityTrackers.get(player.getId());
        if (trackedPlayer != null) {
            trackedPlayer.stopTracking(observer);
            trackedPlayer.updateTrackedStatus(observer);
        }
    }

    private static void sendPacketsToOnlinePlayers(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity observer) {
        /* Update the game profile in player list. */
        observer.networkHandler.sendPacket(new PlayerRemoveS2CPacket(Collections.singletonList(player.getUuid())));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LISTED, player));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
    }
}
