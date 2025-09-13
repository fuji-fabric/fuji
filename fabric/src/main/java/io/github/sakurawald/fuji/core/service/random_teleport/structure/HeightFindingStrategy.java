package io.github.sakurawald.fuji.core.service.random_teleport.structure;

import io.github.sakurawald.fuji.core.service.random_teleport.PositionYSearcher;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum HeightFindingStrategy implements HeightFinder {
    TOP_DOWN((chunk, blockPosX, blockPosZ) -> PositionYSearcher.findYTopBottom(chunk, blockPosX, blockPosZ, Direction.DOWN)),
    DOWN_TOP((chunk, blockPosX, blockPosZ) -> PositionYSearcher.findYTopBottom(chunk, blockPosX, blockPosZ, Direction.UP));

    @SuppressWarnings("ImmutableEnumChecker")
    private final HeightFinder heightFinder;

    HeightFindingStrategy(HeightFinder heightFinder) {
        this.heightFinder = heightFinder;
    }

    public static @NotNull HeightFindingStrategy forWorld(@NotNull ServerWorld world) {
        Optional<RegistryKey<DimensionType>> dimensionTypeRegistryKey = world.getDimensionEntry().getKey();
        return dimensionTypeRegistryKey
            .map(it -> {
                if (it == DimensionTypes.OVERWORLD || it == DimensionTypes.THE_END) {
                    return HeightFindingStrategy.TOP_DOWN;
                }
                if (it == DimensionTypes.THE_NETHER) {
                    return HeightFindingStrategy.DOWN_TOP;
                }
                return null;
            })
            .orElse(HeightFindingStrategy.TOP_DOWN);
    }

    @Override
    public Optional<Integer> getY(Chunk chunk, int x, int z) {
        return heightFinder.getY(chunk, x, z);
    }
}
