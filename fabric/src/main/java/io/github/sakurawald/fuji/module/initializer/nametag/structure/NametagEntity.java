package io.github.sakurawald.fuji.module.initializer.nametag.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PacketHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.nametag.NametagInitializer;
import io.github.sakurawald.fuji.module.initializer.nametag.service.NametagService;
import java.util.List;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
    public boolean shouldRemove() {
        if (this.ownerPlayer.isRemoved()) return true;
        if (this.isRemoved()) return true;

        return false;
    }

    public void setRemoved() {
        PacketHelper.sendPacketToAll(new EntitiesDestroyS2CPacket(this.getId()));
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    private void setDisplayFlag(byte flag, boolean value) {
        DataTracker dataTracker = this.getDataTracker();
        byte oldValue = dataTracker.get(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS);
        byte newValue = EntityHelper.withFlagValue(oldValue, flag, value);
        dataTracker.set(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS, newValue);
    }

    public void update() {
        /* Update properties of the nametag entity. */
        var config = NametagInitializer.config.model();

        // Set billboard mode.
        setBillboardMode(BillboardMode.CENTER);

        // Set text.
        Text text = TextHelper.getTextByValue(this.ownerPlayer, config.style.text);
        this.setText(text);

        // Set translation.
        getDataTracker().set(TRANSLATION, new Vector3f(config.style.offset.x, config.style.offset.y, config.style.offset.z));

        // Set extension.
        setDisplayWidth(config.style.size.width);
        setDisplayHeight(config.style.size.height);

        // Set color.
        setBackground(config.style.color.background);
        setTextOpacity(config.style.color.text_opacity);
        if (config.style.brightness.override_brightness) {
            setBrightness(new Brightness(config.style.brightness.block, config.style.brightness.sky));
        }

        // Set scale.
        getDataTracker().set(SCALE, new Vector3f(config.style.scale.x, config.style.scale.y, config.style.scale.z));

        // Set shadow.
        setDisplayFlag(SHADOW_FLAG, config.style.shadow.shadow);
        setShadowRadius(config.style.shadow.shadow_radius);
        setShadowStrength(config.style.shadow.shadow_strength);

        // Set view distance.
        setDisplayFlag(SEE_THROUGH_FLAG, config.render.see_through_blocks);
        setViewRange(config.render.view_range);

        /* Send entity tracker update packet. */
        List<DataTracker.SerializedEntry<?>> dirtyEntries = this.getDataTracker().getDirtyEntries();
        if (dirtyEntries != null) {
            EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket = new EntityTrackerUpdateS2CPacket(this.getId(), dirtyEntries);
            PacketHelper.sendPacketToAll(entityTrackerUpdateS2CPacket);
        }
    }

    @Override
    public void tick() {
        NametagService.getNametagEntityRemovedReason(this.ownerPlayer)
            .ifPresent(reason -> {
                LogUtil.debug("Discard nametag entity {}: {}", this, reason);
                this.setRemoved();
            });
    }


}
