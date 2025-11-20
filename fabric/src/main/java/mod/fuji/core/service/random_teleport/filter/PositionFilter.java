package mod.fuji.core.service.random_teleport.filter;

import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class PositionFilter {

    public static final Set<Block> KNOWN_DANGEROUS_BLOCKS = Set.of(
        Blocks.POWDER_SNOW,
        Blocks.MAGMA_BLOCK,
        Blocks.FIRE,
        Blocks.SWEET_BERRY_BUSH,
        Blocks.CACTUS,
        Blocks.VOID_AIR,
        Blocks.CAMPFIRE
    );

    public static boolean isYInRange(@NotNull RandomTeleportSettings settings, @NotNull BlockPos blockPos) {
        return blockPos.getY() >= settings.getMinY()
            && blockPos.getY() <= settings.getMaxY();
    }

    public static boolean isInsideWorldBorder(@NotNull ServerLevel world, @NotNull BlockPos blockPos) {
        return world.getWorldBorder().isWithinBounds(blockPos);
    }

    public static boolean isSafeBlock(@NotNull RandomTeleportSettings settings, @NotNull BlockState blockState) {
        String blockId = RegistryHelper.getIdAsString(blockState);
        return isSafeBlock(blockState)
            && !settings.getBlocks().getSkip().contains(blockId);
    }

    public static boolean isSafeBlock(@NotNull BlockState blockState) {
        return !isLiquidBlock(blockState)
            && !KNOWN_DANGEROUS_BLOCKS.contains(blockState.getBlock());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isLiquidBlock(@NotNull BlockState blockState) {
        return !blockState.getFluidState().isEmpty();
    }
}
