package mod.fuji.module.initializer.world.border.structure;

import mod.fuji.core.document.annotation.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.border.WorldBorder;

@Document(id = 1752567811335L, value = """
    Used to describe the `border` of a `dimension`.
    """)
@Data
@NoArgsConstructor
public class BorderDescriptor {

    public boolean enable = true;
    public String dimensionId;
    public Border border = new Border();

    public BorderDescriptor(String dimensionId) {
        this.dimensionId = dimensionId;
    }

    @Data
    @NoArgsConstructor
    public static class Border {
        public double centerX = 0.0;
        public double centerZ = 0.0;
        public double size = 9999968E7;
        public long sizeLerpTime = 0L;
        public double sizeLerpTarget = 0.0;
        public int warningBlocks = 5;
        public int warningTime = 15;
        public double damagePerBlock = 0.2;
        public double safeZone = 5.0;
    }

    private transient WorldBorder vanillaWorldBorder;

    public WorldBorder asVanillaWorldBorder() {
        if (this.vanillaWorldBorder == null) {
            WorldBorder worldBorder = new WorldBorder();
            WorldBorder.Properties properties = new WorldBorder.Properties(this.border.centerX, this.border.centerZ, this.border.damagePerBlock, this.border.safeZone, this.border.warningBlocks, this.border.warningTime, this.border.size, this.border.sizeLerpTime, this.border.sizeLerpTarget);
            worldBorder.load(properties);
            this.vanillaWorldBorder = worldBorder;
        }

        return this.vanillaWorldBorder;
    }

}
