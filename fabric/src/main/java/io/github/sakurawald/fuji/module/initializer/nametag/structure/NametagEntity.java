package io.github.sakurawald.fuji.module.initializer.nametag.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.nametag.NametagInitializer;
import io.github.sakurawald.fuji.module.initializer.nametag.config.model.NametagConfigModel;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@SuppressWarnings("LombokGetterMayBeUsed")
public class NametagEntity extends DisplayEntity.TextDisplayEntity {

    @Getter
    final ServerPlayerEntity ownerPlayer;

    private NametagEntity(@NotNull EntityType<?> entityType, @NotNull World world, @NotNull ServerPlayerEntity ownerPlayer) {
        super(entityType, world);
        this.ownerPlayer = ownerPlayer;
    }

    public static @NotNull NametagEntity make(@NotNull ServerPlayerEntity player) {
        LogUtil.debug("Make nametag for player: {}", PlayerHelper.getPlayerName(player));

        /* Subclassing the display entity to make nametag entity. */
        NametagEntity nametagEntity = new NametagEntity(EntityType.TEXT_DISPLAY, EntityHelper.getServerWorld(player), player);

        /* Make the nametag entity invulnerable. */
        nametagEntity.setInvulnerable(true);

        return nametagEntity;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean isInvalid() {
        if (this.ownerPlayer.isRemoved()) return true;
        if (this.isRemoved()) return true;

        return false;
    }

    public void removeNametag() {
        PacketHelper.sendPacketToAll(new EntitiesDestroyS2CPacket(this.getId()));
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    private void setDisplayFlag(byte flag, boolean value) {
        DataTracker dataTracker = this.getDataTracker();
        byte oldValue = dataTracker.get(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS);
        byte newValue = EntityHelper.withFlagValue(oldValue, flag, value);
        dataTracker.set(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS, newValue);
    }

    public static Optional<String> getNametagDiscardReason(@NotNull ServerPlayerEntity ownerPlayer) {
        if (ownerPlayer.isDead()) return Optional.of("The entity is dead.");
        if (ownerPlayer.isSneaking()) return Optional.of("The entity is sneaking.");

        // NOTE: when the player jumps into the ender portal in the end, its world is minecraft:overworld, its removal reason is `CHANGED_DIMENSION`
        if (ownerPlayer.getRemovalReason() != null) return Optional.of("The entity is removed.");
        if (ownerPlayer.isInvisible()) return Optional.of("The entity is invisible.");

        return Optional.empty();
    }

    public void update() {
        /* Update properties of the nametag entity. */
        var config = NametagInitializer.config.model();

        setBillboardMode(BillboardMode.CENTER);

        Text text = TextHelper.getTextByValue(this.ownerPlayer, config.style.text);
        this.setText(text);

        getDataTracker().set(TRANSLATION, new Vector3f(config.style.offset.x, config.style.offset.y, config.style.offset.z));

        setDisplayWidth(config.style.size.width);
        setDisplayHeight(config.style.size.height);

        setBackground(config.style.color.background);
        setTextOpacity(config.style.color.text_opacity);

        getDataTracker().set(SCALE, new Vector3f(config.style.scale.x, config.style.scale.y, config.style.scale.z));

        setDisplayFlag(SHADOW_FLAG, config.style.shadow.shadow);
        setShadowRadius(config.style.shadow.shadow_radius);
        setShadowStrength(config.style.shadow.shadow_strength);

        setDisplayFlag(SEE_THROUGH_FLAG, config.render.see_through_blocks);
        setViewRange(config.render.view_range);

        if (config.style.brightness.override_brightness) {
            setBrightness(new Brightness(config.style.brightness.block, config.style.brightness.sky));
        }
    }

    @Override
    public void tick() {
        /* Call super to tick the default logic of nametag entity. */
        super.tick();

        ServerPlayerEntity ownerPlayer = this.getOwnerPlayer();
        if (ownerPlayer == null) return;

        /* Calculate the position relative to the player. */
        Vec3d playerPos = ownerPlayer.getPos();
        NametagConfigModel config = NametagInitializer.config.model();

        /* Tick interpolator. */
        if (getInterpolator() == null) {
            LogUtil.warn("Failed to interpolate the nametag entity, the return value of getInterpolator() is null.");
            return;
        }
        getInterpolator().refreshPositionAndAngles(playerPos, this.ownerPlayer.getYaw(), this.ownerPlayer.getPitch());
        getDataTracker().set(DisplayEntity.START_INTERPOLATION, 0);
        getDataTracker().set(DisplayEntity.TELEPORT_DURATION, config.interpolator.duration.interpolate_duration);

        /* Send entity tracker update packet. */
        PlayerPosition playerPosition = new PlayerPosition(playerPos, Vec3d.ZERO, ownerPlayer.getYaw(), ownerPlayer.getPitch());
        List<DataTracker.SerializedEntry<?>> dirtyEntries = this.getDataTracker().getDirtyEntries();
        if (dirtyEntries != null) {
            EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket = new EntityTrackerUpdateS2CPacket(this.getId(), dirtyEntries);
            PacketHelper.sendPacketToAll(entityTrackerUpdateS2CPacket);
        }

        /* Send entity position update packet. */
        EntityPositionSyncS2CPacket positionSyncS2CPacket = new EntityPositionSyncS2CPacket(this.getId(), playerPosition, false);
        PacketHelper.sendPacketToAll(positionSyncS2CPacket);

        /* Discard nametag if the vehicle is sneaking */
        getNametagDiscardReason(this.ownerPlayer)
            .ifPresent(reason -> {
                LogUtil.debug("Discard nametag entity {}: {}", this, reason);
                this.removeNametag();
            });
    }


}
