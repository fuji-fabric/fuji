package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class BlockPosFilter {

    public static final Set<Block> KNOWN_DANGEROUS_BLOCKS = Set.of(
        Blocks.POWDER_SNOW,
        Blocks.MAGMA_BLOCK,
        Blocks.FIRE,
        Blocks.SWEET_BERRY_BUSH,
        Blocks.CACTUS,
        Blocks.VOID_AIR,
        Blocks.CAMPFIRE
    );

    public static boolean isSatisfied(@NotNull RandomTeleportSettings settings, @NotNull Chunk chunk, @NotNull BlockPos blockPos) {
        BlockState blockState = chunk.getBlockState(blockPos);
        return blockPos.getY() >= settings.getMinY()
            && blockPos.getY() <= settings.getMaxY()
            && isSafeBlock(blockState)
            && blockPos.getY() >= chunk.getBottomY()
            && blockPos.getY() <= WorldHelper.getTopY(chunk);
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
