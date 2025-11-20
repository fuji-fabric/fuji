package mod.fuji.module.initializer.nametag.structure;

import com.google.errorprone.annotations.Keep;
import java.util.Optional;
import mod.fuji.core.auxiliary.minecraft.PacketHelper;
import mod.fuji.module.initializer.nametag.service.NametagService;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class NametagEntitySyncer {

    public static void syncNametagEntityToClientWorld(@NotNull NametagEntity nametagEntity) {
        syncExistingNametagEntities(nametagEntity.getOwnerPlayer());
        broadcastNewlyNametagEntityToAllPlayers(nametagEntity);
    }

    private static @NotNull ClientboundSetPassengersPacket makeEntityPassengerSetPacket(@NotNull NametagEntity nametagEntity) {
        ServerPlayer ownerPlayer = nametagEntity.getOwnerPlayer();
        return new ClientboundSetPassengersPacket(ownerPlayer);
    }

    @Keep
    private static void syncExistingNametagEntities(@NotNull ServerPlayer audience) {
        NametagService.nametagEntityMap.forEach((key, value) -> {
            ClientboundAddEntityPacket entitySpawnS2CPacket = makeNametagEntitySpawnPacket(value);
            audience.connection.send(entitySpawnS2CPacket);

            ClientboundSetPassengersPacket entityPassengersSetS2CPacket = makeEntityPassengerSetPacket(value);
            audience.connection.send(entityPassengersSetS2CPacket);

            Optional
                .ofNullable(value.getEntityData().getNonDefaultValues())
                .ifPresent(changedEntries -> {
                    ClientboundSetEntityDataPacket entityTrackerUpdateS2CPacket = new ClientboundSetEntityDataPacket(value.getId(), changedEntries);
                    audience.connection.send(entityTrackerUpdateS2CPacket);
                });
        });
    }

    private static @NotNull ClientboundAddEntityPacket makeNametagEntitySpawnPacket(@NotNull NametagEntity nametagEntity) {
        /* Spawn the nametag over the player's head, so that the player won't see the nametag ride animation. */
        BlockPos blockPos = nametagEntity.getOwnerPlayer().blockPosition().offset(0, 3, 0);

        /* Make entity spawn packet. */
        return new ClientboundAddEntityPacket(nametagEntity, 0, blockPos);
    }

    private static void broadcastNewlyNametagEntityToAllPlayers(@NotNull NametagEntity nametagEntity) {
        /* Spawn entity packet */
        ClientboundAddEntityPacket entitySpawnS2CPacket = makeNametagEntitySpawnPacket(nametagEntity);
        PacketHelper.sendPacketToAll(entitySpawnS2CPacket);

        /* Ride entity packet */
        ClientboundSetPassengersPacket entityPassengersSetS2CPacket = makeEntityPassengerSetPacket(nametagEntity);
        PacketHelper.sendPacketToAll(entityPassengersSetS2CPacket);
    }

}
