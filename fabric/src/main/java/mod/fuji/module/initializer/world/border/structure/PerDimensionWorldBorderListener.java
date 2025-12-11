package mod.fuji.module.initializer.world.border.structure;

import mod.fuji.module.initializer.world.border.WorldBorderInitializer;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.border.BorderChangeListener;
import org.jetbrains.annotations.NotNull;

public class PerDimensionWorldBorderListener implements BorderChangeListener {

    @Override
    public void
    #if MC_VER < MC_1_21_9
    onBorderSizeSet
    #elif MC_VER >= MC_1_21_9
    onSetSize
    #endif
    (@NotNull WorldBorder worldBorder, double d) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderSizePacket(dimension.getWorldBorder()));
    }

    @Override
    public void
    #if MC_VER < MC_1_21_9
    onBorderSizeLerping(@NotNull WorldBorder worldBorder, double d, double e, long l)
    #elif MC_VER >= MC_1_21_9 && MC_VER < MC_1_21_11
    onLerpSize(@NotNull WorldBorder worldBorder, double d, double e, long l)
    #elif MC_VER >= MC_1_21_11
    onLerpSize(WorldBorder worldBorder, double d, double e, long l, long m)
    #endif {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderLerpSizePacket(dimension.getWorldBorder()));
    }

    @Override
    public void
    #if MC_VER < MC_1_21_9
    onBorderCenterSet
    #elif MC_VER >= MC_1_21_9
    onSetCenter
    #endif
    (@NotNull WorldBorder worldBorder, double d, double e) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderCenterPacket(dimension.getWorldBorder()));
    }

    @Override
    public void
    #if MC_VER < MC_1_21_9
    onBorderSetWarningTime
    #elif MC_VER >= MC_1_21_9
    onSetWarningTime
    #endif
    (@NotNull WorldBorder worldBorder, int i) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderWarningDelayPacket(dimension.getWorldBorder()));
    }

    @Override
    public void
    #if MC_VER < MC_1_21_9
    onBorderSetWarningBlocks
    #elif MC_VER >= MC_1_21_9
    onSetWarningBlocks
    #endif
    (@NotNull WorldBorder worldBorder, int i) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderWarningDistancePacket(dimension.getWorldBorder()));
    }

    @Override
    public void
    #if MC_VER < MC_1_21_9
    onBorderSetDamagePerBlock
    #elif MC_VER >= MC_1_21_9
    onSetDamagePerBlock
    #endif
    (@NotNull WorldBorder worldBorder, double d) {}

    @Override
    public void
    #if MC_VER < MC_1_21_9
    onBorderSetDamageSafeZOne
    #elif MC_VER >= MC_1_21_9
    onSetSafeZone
    #endif
    (@NotNull WorldBorder worldBorder, double d) {}
}
