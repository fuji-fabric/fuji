package io.github.sakurawald.fuji.module.initializer.nametag.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.module.initializer.nametag.NametagInitializer;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class NametagEntitySyncer {

    public static void syncNametagEntity(@NotNull NametagEntity nametagEntity) {
        syncExistingNametagEntities(nametagEntity.getOwnerPlayer());
        broadcastNewNametagEntityToAllPlayers(nametagEntity);
    }

    private static @NotNull EntityPassengersSetS2CPacket makeEntityPassengerSetPacket(@NotNull NametagEntity nametagEntity) {
        ServerPlayerEntity ownerPlayer = nametagEntity.getOwnerPlayer();
        int entityId = ownerPlayer.getId();
        List<Entity> list = ownerPlayer.getPassengerList();
        int[] passengerIds = new int[list.size() + 1];
        for (int i = 0; i < list.size(); ++i) {
            passengerIds[i] = list.get(i).getId();
        }
        passengerIds[passengerIds.length - 1] = nametagEntity.getId();

        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        packetByteBuf.writeVarInt(entityId);
        packetByteBuf.writeIntArray(passengerIds);
        EntityPassengersSetS2CPacket packet = new EntityPassengersSetS2CPacket(packetByteBuf);
        return packet;
    }

    private static void syncExistingNametagEntities(@NotNull ServerPlayerEntity audience) {
        NametagInitializer.player2nametag.forEach((key, value) -> {
            BlockPos blockPos = computeNametagEntitySpawnBlockPos(key);
            EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(value, 0, blockPos);
            audience.networkHandler.sendPacket(entitySpawnS2CPacket);

            EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = makeEntityPassengerSetPacket(value);
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
        EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = makeEntityPassengerSetPacket(nametagEntity);
        PacketHelper.sendPacketToAll(entityPassengersSetS2CPacket);
    }

    private static @NotNull BlockPos computeNametagEntitySpawnBlockPos(@NotNull ServerPlayerEntity ownerPlayer) {
        // Spawn the nametag over the player's head, so that the player won't see the nametag ride animation.
        return ownerPlayer.getBlockPos().add(0, 3, 0);
    }
}
