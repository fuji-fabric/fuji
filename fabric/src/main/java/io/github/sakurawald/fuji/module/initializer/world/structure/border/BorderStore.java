package io.github.sakurawald.fuji.module.initializer.world.structure.border;

import net.minecraft.world.border.WorldBorder;

public class BorderStore {

    public double centerX = 0.0;
    public double centerZ = 0.0;
    public double size = 9999968E7;
    public long sizeLerpTime = 0L;
    public double sizeLerpTarget = 0.0;
    public int warningBlocks = 5;
    public int warningTime = 15;
    public double damagePerBlock = 0.2;
    public double safeZone = 5.0;

    public WorldBorder.Properties toWorldBorderProperties() {
        return new WorldBorder.Properties(this.centerX, this.centerZ, this.damagePerBlock, this.safeZone, this.warningBlocks, this.warningTime, this.size, this.sizeLerpTime, this.sizeLerpTarget);
    }

    public void setWorldBorderProperties(WorldBorder.Properties properties) {
        this.centerX = properties.getCenterX();
        this.centerZ = properties.getCenterZ();
        this.size = properties.getSize();
        this.sizeLerpTime = properties.getSizeLerpTime();
        this.sizeLerpTarget = properties.getSizeLerpTarget();
        this.warningBlocks = properties.getWarningBlocks();
        this.warningTime = properties.getWarningTime();
        this.damagePerBlock = properties.getDamagePerBlock();
        this.safeZone = properties.getSafeZone();
    }

}
