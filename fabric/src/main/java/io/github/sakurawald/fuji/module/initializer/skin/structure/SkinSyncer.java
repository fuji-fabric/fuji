package io.github.sakurawald.fuji.module.initializer.skin.structure;

#if MC_VER <= MC_1_20_1
import net.minecraft.world.biome.source.BiomeAccess;
#endif

#if MC_VER > MC_1_21
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import java.util.Collections;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerPosition;
import java.util.Set;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
#endif

public class SkinSyncer {

    public static void broadcastGameProfileChange(@NotNull ServerPlayerEntity player) {
        EntityHelper
            .getServerWorld(player)
            .getPlayers()
            .forEach(observer -> {
                sendPacketsToOnlinePlayers(player, observer);

                if (player != observer && observer.canSee(player)) {
                    sendPacketsToObservingPlayers(player, observer);
                } else if (player == observer) {
                    sendPacketsToSelfPlayer(player, observer);
                }
            });
    }

    private static void sendPacketsToSelfPlayer(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity observer) {
        #if MC_VER <= MC_1_20_1
        observer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.getWorld().getDimensionKey(), player.getWorld().getRegistryKey(), BiomeAccess.hashSeed(player.getServerWorld().getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), player.getWorld().isDebugWorld(), player.getServerWorld().isFlat(), (byte) 2, player.getLastDeathPos(), player.getPortalCooldown()));
        #elif MC_VER > MC_1_20_1
        observer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(EntityHelper.getServerWorld(player)), (byte) 2));
        #endif

        observer.networkHandler.requestTeleport(observer.getX(), observer.getY(), observer.getZ(), observer.getYaw(), observer.getPitch());

        observer.networkHandler.sendPacket(new DifficultyS2CPacket(EntityHelper.getServerWorld(observer).getDifficulty(), EntityHelper.getServerWorld(player).getLevelProperties().isDifficultyLocked()));

        #if MC_VER < MC_1_21_5
        observer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(observer.getInventory().selectedSlot));
        #elif MC_VER >= MC_1_21_5
        observer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(observer.getInventory().getSelectedSlot()));
        #endif


        observer.sendAbilitiesUpdate();
        observer.playerScreenHandler.updateToClient();
        for (StatusEffectInstance instance : observer.getStatusEffects()) {
        #if MC_VER <= MC_1_20_4
        observer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(observer.getId(), instance));
        #elif MC_VER > MC_1_20_4
        observer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(observer.getId(), instance, false));
        #endif
        }

        observer.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
        observer.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        observer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(observer));
    }

    private static void sendPacketsToObservingPlayers(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity observer) {
        observer.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(player.getId()));
        observer.networkHandler.sendPacket(new EntitySpawnS2CPacket(player, 0, player.getBlockPos()));

        #if MC_VER <= MC_1_21
        observer.networkHandler.sendPacket(new EntityPositionS2CPacket(player));
        #elif MC_VER > MC_1_21
        observer.networkHandler.sendPacket(EntityPositionS2CPacket.create(player.getId(), PlayerPosition.fromEntity(player), Set.of(), player.isOnGround()));
        #endif

        observer.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
        observer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
    }

    private static void sendPacketsToOnlinePlayers(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity observer) {
        /* Update the game profile in player list. */
        observer.networkHandler.sendPacket(new PlayerRemoveS2CPacket(Collections.singletonList(player.getUuid())));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LISTED, player));
        observer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
    }
}
