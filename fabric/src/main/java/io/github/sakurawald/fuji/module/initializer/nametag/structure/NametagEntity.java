package io.github.sakurawald.fuji.module.initializer.nametag.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.nametag.NametagInitializer;
import java.util.Optional;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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

    public void invalidate() {
        this.stopRiding();
    }

    private void setDisplayFlag(byte flag, boolean value) {
        DataTracker dataTracker = this.getDataTracker();
        byte oldValue = dataTracker.get(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS);
        byte newValue = EntityHelper.withFlagValue(oldValue, flag, value);
        dataTracker.set(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS, newValue);
    }

    private void discardNametag() {
        PacketHelper.sendPacketToAll(new EntitiesDestroyS2CPacket(this.getId()));
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    public static Optional<String> getNametagDiscardReason(@NotNull LivingEntity entity) {
        if (entity.isDead()) return Optional.of("The entity is dead.");
        if (entity.isSneaking()) return Optional.of("The entity is sneaking.");

        // NOTE: when the player jumps into the ender portal in the end, its world is minecraft:overworld, its removal reason is `CHANGED_DIMENSION`
        if (entity.getRemovalReason() != null) return Optional.of("The entity is removed.");
        if (entity.isInvisible()) return Optional.of("The entity is invisible.");

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

        /* Send update properties packet. */
        if (getDataTracker().isDirty()) {
            var dirty = getDataTracker().getDirtyEntries();
            if (dirty != null) {
                int entityId = getId();
                PacketHelper.sendPacketToAll(new EntityTrackerUpdateS2CPacket(entityId, dirty));
            }
        }
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
        getNametagDiscardReason((LivingEntity) this.getVehicle())
            .ifPresent(reason -> {
                LogUtil.debug("Discard nametag entity {}: {}", this, reason);
                this.discardNametag();
            });
    }

}
