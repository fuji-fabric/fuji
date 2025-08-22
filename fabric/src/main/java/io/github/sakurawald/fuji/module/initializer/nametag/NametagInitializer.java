package io.github.sakurawald.fuji.module.initializer.nametag;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.nametag.config.model.NametagConfigModel;
import io.github.sakurawald.fuji.module.initializer.nametag.job.UpdateNametagJob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private static Map<ServerPlayerEntity, DisplayEntity.TextDisplayEntity> player2nametag;

    private static DisplayEntity.TextDisplayEntity makeNametag(ServerPlayerEntity player) {
        LogUtil.debug("Make nametag for player: {}", player.getGameProfile().getName());

        DisplayEntity.TextDisplayEntity nametag = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, EntityHelper.getServerWorld(player)) {

            private void discardNametag() {
                PacketHelper.sendPacketToAll(new EntitiesDestroyS2CPacket(this.getId()));
                this.remove(RemovalReason.DISCARDED);
            }

            @Override
            public void tick() {
                /* Call super to tick the default logic of nametag entity. */
                super.tick();

                /* Discard nametag if the vehicle is empty */
                if (this.getVehicle() == null) {
                    LogUtil.debug("Discard nametag entity {}: its vehicle is null", this);
                    this.discardNametag();
                    return;
                }

                /* Discard nametag if the vehicle is sneaking */
                String discardNametagReason = getNametagDiscardReason((LivingEntity) this.getVehicle());
                if (discardNametagReason != null) {
                    LogUtil.debug("Discard nametag entity {}: {}", this, discardNametagReason);
                    this.discardNametag();
                }

            }
        };

        /* Make the nametag entity invulnerable. */
        nametag.setInvulnerable(true);

        /* let the nametag riding in internal server-side, so that the server will handle the position update for nametags. */
        letTheNametagRideThePlayer(nametag, player);

        /* send packet to client */
        sendExistingNametagsToTheNewJoinedPlayer(player);
        broadcastTheNewNametagToAllPlayers(player, nametag);
        return nametag;
    }

    private static void letTheNametagRideThePlayer(Entity nametag, PlayerEntity player) {
        // NOTE: the startRiding() method will block the player using nether portal and the end portal.
        nametag.setPose(EntityPose.STANDING);
        nametag.vehicle = player;
        nametag.vehicle.addPassenger(nametag);
    }

    private static BlockPos computeNametagSpawnBlockPos(ServerPlayerEntity bindingPlayer) {
        // spawn the nametag over the player's head, so that the player won't see the nametag ride animation.
        return bindingPlayer.getBlockPos().add(0, 3, 0);
    }

    private static void sendExistingNametagsToTheNewJoinedPlayer(ServerPlayerEntity player) {
        player2nametag.forEach((key, value) -> {
            BlockPos blockPos = computeNametagSpawnBlockPos(key);
            EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(value, 0, blockPos);
            player.networkHandler.sendPacket(entitySpawnS2CPacket);

            EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = new EntityPassengersSetS2CPacket(key);
            player.networkHandler.sendPacket(entityPassengersSetS2CPacket);

            player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(value.getId(), value.getDataTracker().getChangedEntries()));
        });
    }

    private static void broadcastTheNewNametagToAllPlayers(ServerPlayerEntity player, DisplayEntity.TextDisplayEntity textDisplayEntity) {
        /* Spawn entity packet */
        BlockPos blockPos = computeNametagSpawnBlockPos(player);
        EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(textDisplayEntity, 0, blockPos);
        PacketHelper.sendPacketToAll(entitySpawnS2CPacket);

        /* Ride entity packet */
        EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = new EntityPassengersSetS2CPacket(player);
        PacketHelper.sendPacketToAll(entityPassengersSetS2CPacket);
    }

    private static byte setTextDisplayFlags(int base, int flag, boolean value) {
        return (byte) (value ? base | flag : base & ~flag);
    }

    private static void setDisplayFlag(DataTracker dataTracker, byte flag, boolean value) {
        Byte original = dataTracker.get(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS);
        dataTracker.set(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS, setTextDisplayFlags(original, flag, value));
    }

    private static void renderNametag(DisplayEntity.TextDisplayEntity nametag, ServerPlayerEntity player) {
        /* Update props of nametag entity */
        var config = NametagInitializer.config.model();

        nametag.setBillboardMode(DisplayEntity.BillboardMode.CENTER);

        Text text = TextHelper.getTextByValue(player, config.style.text);
        nametag.setText(text);

        nametag.getDataTracker().set(DisplayEntity.TRANSLATION, new Vector3f(config.style.offset.x, config.style.offset.y, config.style.offset.z));

        nametag.setDisplayWidth(config.style.size.width);
        nametag.setDisplayHeight(config.style.size.height);

        nametag.setBackground(config.style.color.background);
        nametag.setTextOpacity(config.style.color.text_opacity);

        nametag.getDataTracker().set(DisplayEntity.SCALE, new Vector3f(config.style.scale.x, config.style.scale.y, config.style.scale.z));

        setDisplayFlag(nametag.getDataTracker(), DisplayEntity.TextDisplayEntity.SHADOW_FLAG, config.style.shadow.shadow);
        nametag.setShadowRadius(config.style.shadow.shadow_radius);
        nametag.setShadowStrength(config.style.shadow.shadow_strength);

        setDisplayFlag(nametag.getDataTracker(), DisplayEntity.TextDisplayEntity.SEE_THROUGH_FLAG, config.render.see_through_blocks);
        nametag.setViewRange(config.render.view_range);

        if (config.style.brightness.override_brightness) {
            nametag.setBrightness(new Brightness(config.style.brightness.block, config.style.brightness.sky));
        }

        /* Send update props packet */
        if (nametag.getDataTracker().isDirty()) {
            var dirty = nametag.getDataTracker().getDirtyEntries();
            if (dirty != null) {
                int entityId = nametag.getId();
                PacketHelper.sendPacketToAll(new EntityTrackerUpdateS2CPacket(entityId, dirty));
            }
        }
    }

    private static String getNametagDiscardReason(LivingEntity entity) {
        if (entity.isDead()) return "The entity is dead.";
        if (entity.isSneaking()) return "The entity is sneaking.";

        // NOTE: when the player jumps into the ender portal in the end, its world is minecraft:overworld, its removal reason is `CHANGED_DIMENSION`
        if (entity.getRemovalReason() != null) return "The entity is removed.";
        if (entity.isInvisible()) return "The entity is invisible.";

        return null;
    }

    public static void processNametagsForOnlinePlayers() {
        // Since the virtual entity is not added into the server, so we should call tick() ourselves.
        player2nametag.values().forEach(DisplayEntity::tick);

        // Invalidate keys
        player2nametag.entrySet().removeIf(entry -> entry.getKey().isRemoved() || entry.getValue().isRemoved());

        // Update
        PlayerHelper.Lookup.getOnlinePlayers().forEach(player -> {
            // Should we create the nametag for this player?
            if (getNametagDiscardReason(player) != null) return;

            // Make the nametag if not exists.
            if (!player2nametag.containsKey(player)) {
                player2nametag.put(player, makeNametag(player));
            }

            // Render the nametag.
            DisplayEntity.TextDisplayEntity nametag = player2nametag.get(player);
            renderNametag(nametag, player);
        });
    }

    @Override
    protected void onInitialize() {
        player2nametag = new ConcurrentHashMap<>();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            UpdateNametagJob updateNametagJob = new UpdateNametagJob();
            Managers.getScheduleManager().scheduleJob(updateNametagJob);
        });
    }

    @Override
    protected void onReload() {
        /* discard all existed nametags, since the `text format` may be changed after reload. */
        LogUtil.debug("Discard all the nametags. (The module is reloaded)" );
        player2nametag.forEach((key, value) -> value.stopRiding());
    }

}
