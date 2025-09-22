package mod.fuji.module.initializer.nametag.structure;

import com.google.errorprone.annotations.Keep;
import java.util.Optional;
import mod.fuji.core.auxiliary.minecraft.PacketHelper;
import mod.fuji.module.initializer.nametag.service.NametagService;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class NametagEntitySyncer {

    public static void syncNametagEntityToClientWorld(@NotNull NametagEntity nametagEntity) {
        syncExistingNametagEntities(nametagEntity.getOwnerPlayer());
        broadcastNewlyNametagEntityToAllPlayers(nametagEntity);
    }

    private static @NotNull EntityPassengersSetS2CPacket makeEntityPassengerSetPacket(@NotNull NametagEntity nametagEntity) {
        ServerPlayerEntity ownerPlayer = nametagEntity.getOwnerPlayer();
        return new EntityPassengersSetS2CPacket(ownerPlayer);
    }

    @Keep
    private static void syncExistingNametagEntities(@NotNull ServerPlayerEntity audience) {
        NametagService.nametagEntityMap.forEach((key, value) -> {
            EntitySpawnS2CPacket entitySpawnS2CPacket = makeNametagEntitySpawnPacket(value);
            audience.networkHandler.sendPacket(entitySpawnS2CPacket);

            EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = makeEntityPassengerSetPacket(value);
            audience.networkHandler.sendPacket(entityPassengersSetS2CPacket);

            Optional
                .ofNullable(value.getDataTracker().getChangedEntries())
                .ifPresent(changedEntries -> {
                    EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket = new EntityTrackerUpdateS2CPacket(value.getId(), changedEntries);
                    audience.networkHandler.sendPacket(entityTrackerUpdateS2CPacket);
                });
        });
    }

    private static @NotNull EntitySpawnS2CPacket makeNametagEntitySpawnPacket(@NotNull NametagEntity nametagEntity) {
        /* Spawn the nametag over the player's head, so that the player won't see the nametag ride animation. */
        BlockPos blockPos = nametagEntity.getOwnerPlayer().getBlockPos().add(0, 3, 0);

        /* Make entity spawn packet. */
        return new EntitySpawnS2CPacket(nametagEntity, 0, blockPos);
    }

    private static void broadcastNewlyNametagEntityToAllPlayers(@NotNull NametagEntity nametagEntity) {
        /* Spawn entity packet */
        EntitySpawnS2CPacket entitySpawnS2CPacket = makeNametagEntitySpawnPacket(nametagEntity);
        PacketHelper.sendPacketToAll(entitySpawnS2CPacket);

        /* Ride entity packet */
        EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = makeEntityPassengerSetPacket(nametagEntity);
        PacketHelper.sendPacketToAll(entityPassengersSetS2CPacket);
    }

}
