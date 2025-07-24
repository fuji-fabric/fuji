package io.github.sakurawald.fuji.module.initializer.skin.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import java.util.Collections;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;

#if MC_VER <= MC_1_20_1
import net.minecraft.world.biome.source.BiomeAccess;
#endif

#if MC_VER > MC_1_21
import java.util.Set;
import java.util.Collections;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
#endif

public class SkinSyncer {

    public static void broadcastGameProfileChange(@NotNull ServerPlayerEntity player) {
        ServerHelper.getOnlinePlayers()
            .forEach(observer -> {
                sendPacketsToOnlinePlayers(player, observer);

                if (player == observer) {
                    sendPacketsToSelfPlayer(player);
                } else {
                    sendPacketsToObservingPlayers(player, observer);
                }
            });
    }

    private static void sendPacketsToSelfPlayer(@NotNull ServerPlayerEntity player) {
        // NOTE: This function is used to simulate the PlayerManager#respawnPlayer

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

        /* Restore the previous selected slot. */
        #if MC_VER < MC_1_21_5
        player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));
        #elif MC_VER >= MC_1_21_5
        player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().getSelectedSlot()));
        #endif

        /* Restore the previous inventory slots. */
        player.playerScreenHandler.updateToClient();

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

        /* Update the entity tracker. (Does not harm) */
        player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
    }

    private static void sendPacketsToObservingPlayers(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity observer) {
        /* Re-create the target player. */
        observer.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(player.getId()));
        observer.networkHandler.sendPacket(new EntitySpawnS2CPacket(player.getId(), player.getUuid(), player.getX(), player.getY(), player.getZ(), player.getPitch(), player.getYaw(), player.getType(), 0, player.getVelocity(), player.getHeadYaw()));

        /* Update the position of the target player. (Does not harm) */
        #if MC_VER <= MC_1_21
        observer.networkHandler.sendPacket(new EntityPositionS2CPacket(player));
        #elif MC_VER > MC_1_21
        observer.networkHandler.sendPacket(EntityPositionSyncS2CPacket.create(player));
        #endif

        /* Restore the velocity of the target player. */
        observer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));

        /* Restore the passengers on the target player. */
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            observer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
        }
        observer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));

        /* Restore the tracked data of target player. (Does not harm) */
        observer.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
    }

    private static void sendPacketsToOnlinePlayers(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity observer) {
        /* Update the game profile in player list. */
        observer.networkHandler.sendPacket(new PlayerRemoveS2CPacket(Collections.singletonList(player.getUuid())));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LISTED, player));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
    }
}
