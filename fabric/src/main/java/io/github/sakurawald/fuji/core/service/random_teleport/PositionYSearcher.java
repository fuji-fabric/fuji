package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class PositionYSearcher {

    public static @NotNull Optional<Integer> findYTopBottom(@NotNull Chunk chunk, int blockPosX, int blockPosZ) {
        /* Initialize Y range. */
        final int maxY = WorldHelper.getMaxBlockY(chunk);
        final int minY = WorldHelper.getBottomYInclusive(chunk);
        if (maxY <= minY) {
            return Optional.empty();
        }

        /* Initialize candidate block pos. */
        final BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable(blockPosX, maxY + 2, blockPosZ);
        boolean isAir1 = chunk.getBlockState(mutableBlockPos).isAir(); // Block at head level
        boolean isAir2 = chunk.getBlockState(mutableBlockPos.move(Direction.DOWN)).isAir(); // Block at feet level
        boolean isAir3; // Block below feet

        while (mutableBlockPos.getY() > minY) {
            isAir3 = chunk.getBlockState(mutableBlockPos.move(Direction.DOWN)).isAir();
            if (!isAir3 && isAir2 && isAir1) { // If there is a floor block and space for player body+head
                return Optional.of(mutableBlockPos.getY() + 1);
            }

            isAir1 = isAir2;
            isAir2 = isAir3;
        }

        return Optional.empty();
    }

    private static int getChunkHighestNonEmptySectionYOffsetOrTopY(@NotNull Chunk chunk) {
        int i = chunk.getHighestNonEmptySection();
        return i == WorldHelper.getTopY(chunk) ? chunk.getBottomY() : ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(i));
    }

    @SuppressWarnings("deprecation")
    public static @NotNull Optional<Integer> findYBottomUp(@NotNull Chunk chunk, int x, int z) {
        final int topY = getChunkHighestNonEmptySectionYOffsetOrTopY(chunk);
        final int bottomY = chunk.getBottomY();
        if (topY <= bottomY) {
            return Optional.empty();
        }

        final BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, bottomY, z);
        BlockState bsFeet1 = chunk.getBlockState(mutablePos); // Block below feet
        BlockState bsBody2 = chunk.getBlockState(mutablePos.move(Direction.UP)); // Block at feet level
        BlockState bsHead3; // Block at head level

        while (mutablePos.getY() < topY) {
            bsHead3 = chunk.getBlockState(mutablePos.move(Direction.UP));
            if (bsFeet1.isSolid() && bsBody2.isAir() && bsHead3.isAir()) { // If there is a floor block and space for player body+head
                return Optional.of(mutablePos.getY() - 1);
            }

            bsFeet1 = bsBody2;
            bsBody2 = bsHead3;
        }

        return Optional.empty();
    }

}
