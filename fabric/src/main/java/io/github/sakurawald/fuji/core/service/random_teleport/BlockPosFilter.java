package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class BlockPosFilter {
    static boolean isSatisfied(@NotNull RandomTeleportSettings settings, @NotNull Chunk chunk, @NotNull BlockPos blockPos) {
        BlockState blockState = chunk.getBlockState(blockPos);
        return blockPos.getY() >= settings.getMinY()
            && blockPos.getY() <= settings.getMaxY()
            && blockState.getFluidState().isEmpty()
            && blockState.getBlock() != Blocks.POWDER_SNOW
            && blockState.getBlock() != Blocks.FIRE
            && blockPos.getY() >= chunk.getBottomY()
            && blockPos.getY() <= WorldHelper.getTopY(chunk);
    }
}
