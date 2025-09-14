package io.github.sakurawald.fuji.core.service.random_teleport.searcher;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.service.random_teleport.filter.PositionFilter;
import java.util.Optional;
import net.minecraft.block.BlockState;
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
        BlockState blockState1 = chunk.getBlockState(mutableBlockPos);
        BlockState blockState2 = chunk.getBlockState(mutableBlockPos.move(direction));
        BlockState blockState3;
        if (direction == Direction.DOWN) {
            while (mutableBlockPos.getY() > minY) {
                blockState3 = chunk.getBlockState(mutableBlockPos.move(direction));
                if (!blockState3.isAir() && blockState2.isAir() && blockState1.isAir() && PositionFilter.isSafeBlock(blockState3)) {
                    return Optional.of(mutableBlockPos.getY() + 1);
                }
                blockState1 = blockState2;
                blockState2 = blockState3;
            }
        } else {
            while (mutableBlockPos.getY() < maxY) {
                blockState3 = chunk.getBlockState(mutableBlockPos.move(direction));
                if (!blockState1.isAir() && blockState2.isAir() && blockState3.isAir() && PositionFilter.isSafeBlock(blockState1)) {
                    return Optional.of(mutableBlockPos.getY() - 1);
                }
                blockState1 = blockState2;
                blockState2 = blockState3;
            }
        }

        /* Not lucky. */
        return Optional.empty();
    }

}
