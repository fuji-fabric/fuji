package io.github.sakurawald.fuji.module.initializer.nametag;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.nametag.config.model.NametagConfigModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751825018627L, value = """
    Customize the nametag above the players.
    """)
@ColorBox(id = 1751978505336L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set the background of nametag to blue color.
    Set `background` to `-16776961` (The integer representation of blue color)

    ◉ Set the half transparency for nametag.
    Set `text_opacity` to `128`.

    ◉ Scale the size of text into double.
    Set the `x`, `y`, and `z` in `scale` to `2.0`.
    """)
@TestCase(action = "Pass through a nether portal.", targets = {
    "The nametag entity should be discarded in the old dimension."
    , "A new nametag entity should be created in the new dimension."
})
public class NametagInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<NametagConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, NametagConfigModel.class);

    private static Map<ServerPlayerEntity, NametagEntity> player2nametag;

    private static NametagEntity makeNametag(ServerPlayerEntity player) {
        LogUtil.debug("Make nametag for player: {}", PlayerHelper.getPlayerName(player));
        NametagEntity nametagEntity = NametagEntity.make(player);

        /* let the nametag riding in internal server-side, so that the server will handle the position update for nametags. */
        letTheNametagRideThePlayer(nametagEntity, player);

        /* send packet to client */
        sendExistingNametagsToTheNewJoinedPlayer(player);
        broadcastTheNewNametagToAllPlayers(player, nametagEntity);
        return nametagEntity;
    }

    private static void letTheNametagRideThePlayer(Entity nametag, PlayerEntity player) {
        // NOTE: the startRiding() method will block the player using nether portal and the end portal.
        nametag.setPose(EntityPose.STANDING);
        nametag.vehicle = player;
        nametag.vehicle.addPassenger(nametag);
    }

    private static @NotNull BlockPos computeNametagEntitySpawnBlockPos(@NotNull ServerPlayerEntity bindingPlayer) {
        // Spawn the nametag over the player's head, so that the player won't see the nametag ride animation.
        return bindingPlayer.getBlockPos().add(0, 3, 0);
    }

    private static void sendExistingNametagsToTheNewJoinedPlayer(ServerPlayerEntity player) {
        player2nametag.forEach((key, value) -> {
            BlockPos blockPos = computeNametagEntitySpawnBlockPos(key);
            EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(value, 0, blockPos);
            player.networkHandler.sendPacket(entitySpawnS2CPacket);

            EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = new EntityPassengersSetS2CPacket(key);
            player.networkHandler.sendPacket(entityPassengersSetS2CPacket);

            player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(value.getId(), value.getDataTracker().getChangedEntries()));
        });
    }

    private static void broadcastTheNewNametagToAllPlayers(ServerPlayerEntity player, DisplayEntity.TextDisplayEntity textDisplayEntity) {
        /* Spawn entity packet */
        BlockPos blockPos = computeNametagEntitySpawnBlockPos(player);
        EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(textDisplayEntity, 0, blockPos);
        PacketHelper.sendPacketToAll(entitySpawnS2CPacket);

        /* Ride entity packet */
        EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = new EntityPassengersSetS2CPacket(player);
        PacketHelper.sendPacketToAll(entityPassengersSetS2CPacket);
    }

    public static void processNametagsForOnlinePlayers() {
        // Since the virtual entity is not added into the server, so we should call tick() ourselves.
        player2nametag.values().forEach(DisplayEntity::tick);

        /* Remove invalid nametag entities. */
        player2nametag.values().removeIf(NametagEntity::isInvalid);

        // Update
        PlayerHelper.Lookup.getOnlinePlayers().forEach(player -> {
            // Skip making the nametag entity for the player, if a discard reason is present.
            if (NametagEntity.getNametagDiscardReason(player).isPresent()) return;

            // Make the nametag if not exists.
            NametagEntity nametagEntity = player2nametag.computeIfAbsent(player, key -> makeNametag(player));

            // Render the nametag.
            nametagEntity.update();
        });
    }




    @Override
    protected void onInitialize() {
        player2nametag = new ConcurrentHashMap<>();
    }

    @Override
    protected void onReload() {
        LogUtil.debug("Invalidate all the created nametag entities. (Reason: module reloaded)");
        player2nametag.values().forEach(NametagEntity::invalidate);
    }

}
