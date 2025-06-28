package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import io.github.sakurawald.fuji.core.service.random_teleport.RandomTeleporter;
import io.github.sakurawald.fuji.core.structure.TeleportSetup;
import lombok.experimental.UtilityClass;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionTypes;

@UtilityClass
public class WorldHelper {

    public static void teleportToSafePositionNearOrigin(ServerWorld world, ServerPlayerEntity player) {
        RandomTeleporter.request(player,
            new TeleportSetup(
                RegistryHelper.ofString(world)
                ,0
                , 0
                , false
                , 0
                , 256
                ,0
                , 128
                ,32
        ), null);
    }

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


    public static Item getSensibleWorldItem(String dimension) {
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
