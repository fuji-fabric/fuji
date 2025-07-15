package io.github.sakurawald.fuji.module.initializer.world.border.structure;

import lombok.Data;
import net.minecraft.world.border.WorldBorder;

@Data
public class BorderDescriptor {

    public final String dimensionId;
    public double centerX = 0.0;
    public double centerZ = 0.0;
    public double size = 9999968E7;
    public long sizeLerpTime = 0L;
    public double sizeLerpTarget = 0.0;
    public int warningBlocks = 5;
    public int warningTime = 15;
    public double damagePerBlock = 0.2;
    public double safeZone = 5.0;

    public transient WorldBorder vanillaWorldBorder;

    public WorldBorder asVanillaWorldBorder() {
        if (this.vanillaWorldBorder == null) {
            WorldBorder worldBorder = new WorldBorder();
            WorldBorder.Properties properties = new WorldBorder.Properties(this.centerX, this.centerZ, this.damagePerBlock, this.safeZone, this.warningBlocks, this.warningTime, this.size, this.sizeLerpTime, this.sizeLerpTarget);
            worldBorder.load(properties);
            this.vanillaWorldBorder = worldBorder;
        }

        return this.vanillaWorldBorder;
    }

}
