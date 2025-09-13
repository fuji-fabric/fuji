package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class PositionYSearcher {

    public static @NotNull Optional<Integer> findYTopBottom(@NotNull Chunk chunk, int blockPosX, int blockPosZ, @NotNull Direction direction) {
        /* Initialize Y range. */
        final int minY = WorldHelper.getBottomYInclusive(chunk);
        final int maxY = WorldHelper.getMaxBlockY(chunk);
        if (minY >= maxY) {
            return Optional.empty();
        }

        /* Initialize loop variables. */
        final int PLAYER_HEIGHT = 2;
        final int initialBlockY = (direction == Direction.DOWN ? maxY : minY) + PLAYER_HEIGHT;
        final BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable(blockPosX, initialBlockY, blockPosZ);

        /* Iterate the block stream. */
        boolean isAir1 = chunk.getBlockState(mutableBlockPos).isAir();
        boolean isAir2 = chunk.getBlockState(mutableBlockPos.move(direction)).isAir();
        boolean isAir3;
        if (direction == Direction.DOWN) {
            while (mutableBlockPos.getY() > minY) {
                isAir3 = chunk.getBlockState(mutableBlockPos.move(direction)).isAir();
                if (!isAir3 && isAir2 && isAir1) {
                    return Optional.of(mutableBlockPos.getY() + 1);
                }
                isAir1 = isAir2;
                isAir2 = isAir3;
            }
        } else {
            while (mutableBlockPos.getY() < maxY) {
                isAir3 = chunk.getBlockState(mutableBlockPos.move(direction)).isAir();
                if (!isAir1 && isAir2 && isAir3) {
                    return Optional.of(mutableBlockPos.getY() - 1);
                }
                isAir1 = isAir2;
                isAir2 = isAir3;
            }
        }

        /* Not lucky. */
        return Optional.empty();
    }

}
