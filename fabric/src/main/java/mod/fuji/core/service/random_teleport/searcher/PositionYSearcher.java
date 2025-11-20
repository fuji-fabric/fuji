package mod.fuji.core.service.random_teleport.searcher;

import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.service.random_teleport.filter.PositionFilter;
import java.util.Optional;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.jetbrains.annotations.NotNull;

public abstract class PositionYSearcher {

    @NotNull
    public abstract Optional<Integer> search(@NotNull ChunkAccess chunk, int blockPosX, int blockPosZ);

    public static @NotNull PositionYSearcher forWorld(@NotNull ServerLevel world) {
        Optional<ResourceKey<DimensionType>> dimensionTypeRegistryKey = world.dimensionTypeRegistration().unwrapKey();
        return dimensionTypeRegistryKey
            .map(it -> {
                if (it == BuiltinDimensionTypes.OVERWORLD || it == BuiltinDimensionTypes.END) {
                    return new PositionYTopDownSearcher();
                }
                if (it == BuiltinDimensionTypes.NETHER) {
                    return new PositionYDownTopSearcher();
                }
                return null;
            })
            .orElseGet(PositionYTopDownSearcher::new);
    }

    public static @NotNull Optional<Integer> search(@NotNull ChunkAccess chunk, int blockPosX, int blockPosZ, @NotNull Direction direction) {
        /* Initialize Y range. */
        final int minY = WorldHelper.HeightView.getMinBuildingY(chunk);
        final int maxY = WorldHelper.HeightView.getMaxBlockY(chunk);
        if (minY >= maxY) {
            return Optional.empty();
        }

        /* Initialize loop variables. */
        final int PLAYER_HEIGHT = 2;
        final int initialBlockY = (direction == Direction.DOWN ? maxY : minY) + PLAYER_HEIGHT;
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPosX, initialBlockY, blockPosZ);

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
