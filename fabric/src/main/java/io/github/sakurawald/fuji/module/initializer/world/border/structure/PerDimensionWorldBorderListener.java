package io.github.sakurawald.fuji.module.initializer.world.border.structure;

import io.github.sakurawald.fuji.module.initializer.world.border.WorldBorderInitializer;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;

public class PerDimensionWorldBorderListener implements WorldBorderListener {

    @Override
    public void onSizeChange(WorldBorder worldBorder, double d) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderSizeChangedS2CPacket(dimension.getWorldBorder()));
    }

    @Override
    public void onInterpolateSize(WorldBorder worldBorder, double d, double e, long l) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderInterpolateSizeS2CPacket(dimension.getWorldBorder()));
    }

    @Override
    public void onCenterChanged(WorldBorder worldBorder, double d, double e) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderCenterChangedS2CPacket(dimension.getWorldBorder()));
    }

    @Override
    public void onWarningTimeChanged(WorldBorder worldBorder, int i) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderWarningTimeChangedS2CPacket(dimension.getWorldBorder()));
    }

    @Override
    public void onWarningBlocksChanged(WorldBorder worldBorder, int i) {
        WorldBorderInitializer.sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderWarningBlocksChangedS2CPacket(dimension.getWorldBorder()));
    }

    @Override
    public void onDamagePerBlockChanged(WorldBorder worldBorder, double d) {
    }

    @Override
    public void onSafeZoneChanged(WorldBorder worldBorder, double d) {
    }
}
