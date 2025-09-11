package io.github.sakurawald.fuji.module.initializer.nametag.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.module.initializer.nametag.NametagInitializer;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class NametagEntitySyncer {

    public static void syncNametagEntity(@NotNull NametagEntity nametagEntity) {
        /* Let the nametag riding in internal server-side, so that the server will handle the position update for nametags. */
        letTheNametagRideThePlayer(nametagEntity);

        /* send packet to client */
        syncExistingNametagEntities(nametagEntity.getOwnerPlayer());
        broadcastNewNametagEntityToAllPlayers(nametagEntity);
    }

    private static void letTheNametagRideThePlayer(NametagEntity nametag) {
        // NOTE: the startRiding() method will block the player using nether portal and the end portal.
        nametag.setPose(EntityPose.STANDING);
        nametag.vehicle = nametag.getOwnerPlayer();
        nametag.vehicle.addPassenger(nametag);
    }

    private static void syncExistingNametagEntities(@NotNull ServerPlayerEntity audience) {
        NametagInitializer.player2nametag.forEach((key, value) -> {
            BlockPos blockPos = computeNametagEntitySpawnBlockPos(key);
            EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(value, 0, blockPos);
            audience.networkHandler.sendPacket(entitySpawnS2CPacket);

            EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = new EntityPassengersSetS2CPacket(key);
            audience.networkHandler.sendPacket(entityPassengersSetS2CPacket);

            EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket = new EntityTrackerUpdateS2CPacket(value.getId(), value.getDataTracker().getChangedEntries());
            audience.networkHandler.sendPacket(entityTrackerUpdateS2CPacket);
        });
    }

    private static void broadcastNewNametagEntityToAllPlayers(@NotNull NametagEntity nametagEntity) {
        /* Spawn entity packet */
        ServerPlayerEntity ownerPlayer = nametagEntity.getOwnerPlayer();
        BlockPos blockPos = computeNametagEntitySpawnBlockPos(ownerPlayer);
        EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(nametagEntity, 0, blockPos);
        PacketHelper.sendPacketToAll(entitySpawnS2CPacket);

        /* Ride entity packet */
        EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = new EntityPassengersSetS2CPacket(ownerPlayer);
        PacketHelper.sendPacketToAll(entityPassengersSetS2CPacket);
    }

    private static @NotNull BlockPos computeNametagEntitySpawnBlockPos(@NotNull ServerPlayerEntity bindingPlayer) {
        // Spawn the nametag over the player's head, so that the player won't see the nametag ride animation.
        return bindingPlayer.getBlockPos().add(0, 3, 0);
    }
}
