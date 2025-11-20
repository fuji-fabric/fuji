package mod.fuji.module.initializer.nametag.structure;

import java.util.Optional;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.PacketHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.nametag.NametagInitializer;
import mod.fuji.module.initializer.nametag.service.NametagService;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class NametagEntity extends Display.TextDisplay {

    @Getter
    final ServerPlayer ownerPlayer;

    boolean shouldRenderPreviousValue = false;

    private NametagEntity(@NotNull EntityType<?> entityType, @NotNull Level world, @NotNull ServerPlayer ownerPlayer) {
        super(entityType, world);
        this.ownerPlayer = ownerPlayer;
    }

    public static @NotNull NametagEntity make(@NotNull ServerPlayer player) {
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
        this.remove(Entity.RemovalReason.DISCARDED);
        PacketHelper.sendPacketToAll(new ClientboundRemoveEntitiesPacket(this.getId()));
    }

    private void setDisplayFlag(byte flag, boolean value) {
        SynchedEntityData dataTracker = this.getEntityData();
        byte oldValue = dataTracker.get(Display.TextDisplay.DATA_STYLE_FLAGS_ID);
        byte newValue = EntityHelper.withFlagValue(oldValue, flag, value);
        dataTracker.set(Display.TextDisplay.DATA_STYLE_FLAGS_ID, newValue);
    }

    public void updateTrackedData() {
        /* Update properties of the nametag entity. */
        var config = NametagInitializer.config.model();

        // Set billboard mode.
        setBillboardConstraints(BillboardConstraints.CENTER);

        // Set text.
        Component text = TextHelper.getTextByValue(this.ownerPlayer, config.style.text);
        this.setText(text);

        // Set translation.
        getEntityData().set(DATA_TRANSLATION_ID, new Vector3f(config.style.offset.x, config.style.offset.y, config.style.offset.z));

        // Set extension.
        setWidth(config.style.size.width);
        setHeight(config.style.size.height);

        // Set color.
        setBackgroundColor(config.style.color.background);
        setTextOpacity(config.style.color.text_opacity);
        if (config.style.brightness.override_brightness) {
            setBrightnessOverride(new Brightness(config.style.brightness.block, config.style.brightness.sky));
        }

        // Set scale.
        if (NametagService.shouldRenderNametagEntity(this)) {
            getEntityData().set(DATA_SCALE_ID, new Vector3f(config.style.scale.x, config.style.scale.y, config.style.scale.z));
        } else {
            getEntityData().set(DATA_SCALE_ID, new Vector3f(0, 0, 0));
        }

        // Set shadow.
        setDisplayFlag(FLAG_SHADOW, config.style.shadow.shadow);
        setShadowRadius(config.style.shadow.shadow_radius);
        setShadowStrength(config.style.shadow.shadow_strength);

        // Set view distance.
        setDisplayFlag(FLAG_SEE_THROUGH, config.render.see_through_blocks);
        setViewRange(config.render.view_range);

        /* Send entity tracker update packet. */
        Optional
            .ofNullable(this.getEntityData().packDirty())
            .ifPresent(dirtyEntries -> {
                ClientboundSetEntityDataPacket entityTrackerUpdateS2CPacket = new ClientboundSetEntityDataPacket(this.getId(), dirtyEntries);
                PacketHelper.sendPacketToAll(entityTrackerUpdateS2CPacket);
            });
    }

    @Override
    public void tick() {
        /* Remove self if removal reason is present.*/
        NametagService
            .getNametagEntityRemovalReason(this.ownerPlayer)
            .ifPresent(reason -> {
                LogUtil.debug("Discard nametag entity {}: {}", this, reason);
                this.setRemoved();
            });

        /* Improve the responsiveness of nametag hiding and showing. */
        boolean shouldRenderCurrentValue = NametagService.shouldRenderNametagEntity(this);
        if (shouldRenderCurrentValue != this.shouldRenderPreviousValue) {
            this.shouldRenderPreviousValue = shouldRenderCurrentValue;
            this.updateTrackedData();
        }
    }

}
