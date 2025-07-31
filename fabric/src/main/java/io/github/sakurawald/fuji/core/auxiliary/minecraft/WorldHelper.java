package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionTypes;

public class WorldHelper {

    public static int getTopY(Chunk chunk) {
        #if MC_VER <= MC_1_21
        return chunk.getTopY();
        #elif MC_VER > MC_1_21
        return chunk.getTopYInclusive();
        #endif
    }

    public static int getTopY(World world) {
        #if MC_VER <= MC_1_21
        return world.getTopY();
        #elif MC_VER > MC_1_21
        return world.getTopYInclusive();
        #endif
    }

    public static Vec3d toBottomCenterPos(BlockPos pos) {
        return Vec3d.add(pos, 0.5, 0.0, 0.5);
    }

    public static Item toGuiItem(String dimension) {
        if (dimension.equals(DimensionTypes.OVERWORLD_ID.toString())) {
            return Items.GRASS_BLOCK;
        }

        if (dimension.equals(DimensionTypes.THE_END_ID.toString())) {
            return Items.END_STONE;
        }

        if (dimension.equals(DimensionTypes.THE_NETHER_ID.toString())) {
            return Items.NETHERRACK;
        }

        return Items.ENDER_PEARL;
    }
}
