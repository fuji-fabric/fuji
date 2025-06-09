package io.github.sakurawald.module.initializer.gameplay.multi_obsidian_platform;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.gameplay.multi_obsidian_platform.config.model.MultiObsidianPlatformConfigModel;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class MultiObsidianPlatformInitializer extends ModuleInitializer {

    private static final Map<BlockPos, BlockPos> TRANSFORM_CACHE = new HashMap<>();

    private static final BaseConfigurationHandler<MultiObsidianPlatformConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, MultiObsidianPlatformConfigModel.class);

    /* this method is used to fix Entity#position() async */
    private static BlockPos findNearbyEndPortalBlock(@NotNull BlockPos bp) {
        ServerWorld overworld = ServerHelper.getServer().getOverworld();

        // should we find nearby END_PORTAL block ?
        if (overworld.getBlockState(bp) == Blocks.END_PORTAL.getDefaultState()) return bp;

        // let's find nearby END_PORTAL block
        int radius = 3;
        for (int y = -radius; y < radius; y++) {
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    BlockPos test = bp.add(x, y, z);
                    if (overworld.getBlockState(test) == Blocks.END_PORTAL.getDefaultState()) return test;
                }
            }
        }

        LogUtil.warn("the BlockPos {} is not END_PORTAL and we can't find a nearby END_PORTAL block !", bp);
        return bp;
    }

    private static BlockPos findCenterEndPortalBlock(@NotNull BlockPos bp) {
        ServerWorld overworld = ServerHelper.getServer().getOverworld();
        if (overworld.getBlockState(bp.north()) != Blocks.END_PORTAL.getDefaultState()) {
            if (overworld.getBlockState(bp.west()) != Blocks.END_PORTAL.getDefaultState()) {
                return bp.south().east();
            } else if (overworld.getBlockState(bp.east()) != Blocks.END_PORTAL.getDefaultState()) {
                return bp.south().west();
            }
            return bp.south();
        }
        if (overworld.getBlockState(bp.south()) != Blocks.END_PORTAL.getDefaultState()) {
            if (overworld.getBlockState(bp.west()) != Blocks.END_PORTAL.getDefaultState()) {
                return bp.north().east();
            } else if (overworld.getBlockState(bp.east()) != Blocks.END_PORTAL.getDefaultState()) {
                return bp.north().west();
            }
            return bp.north();
        }
        if (overworld.getBlockState(bp.west()) != Blocks.END_PORTAL.getDefaultState()) {
            return bp.east();
        }
        if (overworld.getBlockState(bp.east()) != Blocks.END_PORTAL.getDefaultState()) {
            return bp.west();
        }
        // This is the center block.
        return bp;
    }

    public static BlockPos transform(BlockPos bp) {
        if (TRANSFORM_CACHE.containsKey(bp)) {
            return TRANSFORM_CACHE.get(bp);
        }
        // fix: for sand-dupe, the blockpos (x, ?, z) of sand may differ +1 or -1
        bp = findNearbyEndPortalBlock(bp);
        bp = findCenterEndPortalBlock(bp);
        double factor = config.model().factor;
        int x = (int) (bp.getX() / factor);
        int y = 50;
        int z = (int) (bp.getZ() / factor);
        int x_offset = x % 16;
        int z_offset = z % 16;
        x -= x_offset;
        z -= z_offset;
        x += 100;
        TRANSFORM_CACHE.put(bp, new BlockPos(x, y, z));
        return TRANSFORM_CACHE.get(bp);
    }

}
