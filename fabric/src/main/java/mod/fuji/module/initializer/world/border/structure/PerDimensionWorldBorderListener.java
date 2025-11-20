package mod.fuji.module.initializer.world.border.structure;

import mod.fuji.module.initializer.world.border.WorldBorderInitializer;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.border.BorderChangeListener;

public class PerDimensionWorldBorderListener implements BorderChangeListener {

    @Override
    public void onSetSize(WorldBorder worldBorder, double d) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderSizePacket(dimension.getWorldBorder()));
    }

    @Override
    public void onLerpSize(WorldBorder worldBorder, double d, double e, long l) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderLerpSizePacket(dimension.getWorldBorder()));
    }

    @Override
    public void onSetCenter(WorldBorder worldBorder, double d, double e) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderCenterPacket(dimension.getWorldBorder()));
    }

    @Override
    public void onSetWarningTime(WorldBorder worldBorder, int i) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderWarningDelayPacket(dimension.getWorldBorder()));
    }

    @Override
    public void onSetWarningBlocks(WorldBorder worldBorder, int i) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderWarningDistancePacket(dimension.getWorldBorder()));
    }

    @Override
    public void onSetDamagePerBlock(WorldBorder worldBorder, double d) {
    }

    @Override
    public void onSetSafeZone(WorldBorder worldBorder, double d) {
    }
}
