package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import java.util.Optional;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

public abstract class PositionYSearcher {

    @NotNull
    public abstract Optional<Integer> search(@NotNull Chunk chunk, int blockPosX, int blockPosZ);

    public static @NotNull PositionYSearcher forWorld(@NotNull ServerWorld world) {
        Optional<RegistryKey<DimensionType>> dimensionTypeRegistryKey = world.getDimensionEntry().getKey();
        return dimensionTypeRegistryKey
            .map(it -> {
                if (it == DimensionTypes.OVERWORLD || it == DimensionTypes.THE_END) {
                    return new PositionYTopDownSearcher();
                }
                if (it == DimensionTypes.THE_NETHER) {
                    return new PositionYDownTopSearcher();
                }
                return null;
            })
            .orElseGet(PositionYTopDownSearcher::new);
    }

    public static @NotNull Optional<Integer> search(@NotNull Chunk chunk, int blockPosX, int blockPosZ, @NotNull Direction direction) {
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
