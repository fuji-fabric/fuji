package mod.fuji.module.initializer.gameplay.multi_obsidian_platform;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.gameplay.multi_obsidian_platform.config.model.MultiObsidianPlatformConfigModel;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Document(id = 1751827009997L, value = """
    This module makes every `ender portal frame` generates its own `obsidian platform`.
    """)
@ColorBox(id = 1751976988699L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ All the obsidian platforms are vanilla-respect.
    That's to say, all the additional `obsidian platforms` have the `identical` behaviour as the vanilla one, which locates in (100, 50, 0).

    ◉ A well-known feature if you create the `Ender Portal` in the nether.
    See https://bugs.mojang.com/browse/MC-252361
    """)
public class MultiObsidianPlatformInitializer extends ModuleInitializer {

    private static final Map<BlockPos, BlockPos> TRANSFORM_CACHE = new ConcurrentHashMap<>();

    private static final BaseConfigurationHandler<MultiObsidianPlatformConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, MultiObsidianPlatformConfigModel.class);

    @ForDeveloper("This method is used to fix Entity#position() drift.")
    private static BlockPos findNearbyEndPortalBlock(@NotNull BlockPos bp) {
        ServerWorld overworld = ServerHelper.getServer().getOverworld();

        /* Should we find nearby END_PORTAL block ? */
        if (overworld.getBlockState(bp) == Blocks.END_PORTAL.getDefaultState()) return bp;

        /* Let's find nearby END_PORTAL block */
        int searchRadius = 3;
        for (int y = -searchRadius; y < searchRadius; y++) {
            for (int x = -searchRadius; x < searchRadius; x++) {
                for (int z = -searchRadius; z < searchRadius; z++) {
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

    public static BlockPos getTransformedEndSpawnPosition(BlockPos enderPortalFrameBlockPos) {
        if (TRANSFORM_CACHE.containsKey(enderPortalFrameBlockPos)) {
            return TRANSFORM_CACHE.get(enderPortalFrameBlockPos);
        }
        // NOTE: For sand-dupe, the blockpos (x, ?, z) of sand may differ +1 or -1
        enderPortalFrameBlockPos = findNearbyEndPortalBlock(enderPortalFrameBlockPos);
        enderPortalFrameBlockPos = findCenterEndPortalBlock(enderPortalFrameBlockPos);
        double factor = config.model().factor;
        int x = (int) (enderPortalFrameBlockPos.getX() / factor);
        int y = 50;
        int z = (int) (enderPortalFrameBlockPos.getZ() / factor);
        int x_offset = x % 16;
        int z_offset = z % 16;
        x -= x_offset;
        z -= z_offset;
        x += 100;
        TRANSFORM_CACHE.put(enderPortalFrameBlockPos, new BlockPos(x, y, z));
        return TRANSFORM_CACHE.get(enderPortalFrameBlockPos);
    }

    @Override
    protected void onReload() {
        // NOTE: Provide a way to clear the cache after the ENDER_PORTAL_FRAME block is broken and re-placed.
        TRANSFORM_CACHE.clear();
    }
}
