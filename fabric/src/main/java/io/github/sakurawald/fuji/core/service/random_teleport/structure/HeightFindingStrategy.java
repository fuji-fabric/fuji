package io.github.sakurawald.fuji.core.service.random_teleport.structure;

import io.github.sakurawald.fuji.core.service.random_teleport.PositionYSearcher;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum HeightFindingStrategy implements HeightFinder {
    SKY_TO_SURFACE__FIRST_SOLID(PositionYSearcher::findYTopBottom),
    BOTTOM_TO_SKY__FIRST_SAFE_AIR(PositionYSearcher::findYBottomUp);

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
                    return HeightFindingStrategy.SKY_TO_SURFACE__FIRST_SOLID;
                }
                if (it == DimensionTypes.THE_NETHER) {
                    return HeightFindingStrategy.BOTTOM_TO_SKY__FIRST_SAFE_AIR;
                }
                return null;
            })
            .orElse(HeightFindingStrategy.SKY_TO_SURFACE__FIRST_SOLID);
    }

    @Override
    public Optional<Integer> getY(Chunk chunk, int x, int z) {
        return heightFinder.getY(chunk, x, z);
    }
}
