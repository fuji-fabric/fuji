package io.github.sakurawald.core.service.random_teleport.structure;

import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;

public enum HeightFindingStrategy implements HeightFinder {
    SKY_TO_SURFACE__FIRST_SOLID(HeightFindingStrategy::findYTopBottom),
    BOTTOM_TO_SKY__FIRST_SAFE_AIR(HeightFindingStrategy::findYBottomUp);

    private final HeightFinder heightFinder;

    HeightFindingStrategy(HeightFinder heightFinder) {
        this.heightFinder = heightFinder;
    }

    private static int computeMaxY(@NotNull Chunk chunk) {
        final int maxY = chunk.getTopYInclusive();
        ChunkSection[] sections = chunk.getSectionArray();
        int maxSectionIndex = Math.min(sections.length - 1, maxY >> 4);

        for (int index = maxSectionIndex; index >= 0; --index) {
            if (!sections[index].isEmpty()) {
                return Math.min(index << 4 + 15, maxY);
            }
        }

        return Integer.MAX_VALUE;
    }

    public static @NotNull HeightFindingStrategy forWorld(@NotNull ServerWorld world) {
        Optional<RegistryKey<DimensionType>> dt = world.getDimensionEntry().getKey();
        if (dt.get() == DimensionTypes.OVERWORLD || dt.get() == DimensionTypes.THE_END) {
            return HeightFindingStrategy.SKY_TO_SURFACE__FIRST_SOLID;
        }
        if (dt.get() == DimensionTypes.THE_NETHER) {
            return HeightFindingStrategy.BOTTOM_TO_SKY__FIRST_SAFE_AIR;
        }

        // fallback
        return HeightFindingStrategy.SKY_TO_SURFACE__FIRST_SOLID;
    }

    public static @NotNull OptionalInt findYTopBottom(@NotNull Chunk chunk, int x, int z) {
        final int maxY = computeMaxY(chunk);
        final int bottomY = chunk.getBottomY();
        if (maxY <= bottomY) {
            return OptionalInt.empty();
        }

        final BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, maxY, z);
        boolean isAir1 = chunk.getBlockState(mutablePos).isAir(); // Block at head level
        boolean isAir2 = chunk.getBlockState(mutablePos.move(Direction.DOWN)).isAir(); // Block at feet level
        boolean isAir3; // Block below feet

        while (mutablePos.getY() > bottomY) {
            isAir3 = chunk.getBlockState(mutablePos.move(Direction.DOWN)).isAir();
            if (!isAir3 && isAir2 && isAir1) { // If there is a floor block and space for player body+head
                return OptionalInt.of(mutablePos.getY() + 1);
            }

            isAir1 = isAir2;
            isAir2 = isAir3;
        }

        return OptionalInt.empty();
    }

    @SuppressWarnings("deprecation")
    private static @NotNull OptionalInt findYBottomUp(@NotNull Chunk chunk, int x, int z) {
        final int topY = getChunkHighestNonEmptySectionYOffsetOrTopY(chunk);
        final int bottomY = chunk.getBottomY();
        if (topY <= bottomY) {
            return OptionalInt.empty();
        }

        final BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, bottomY, z);
        BlockState bsFeet1 = chunk.getBlockState(mutablePos); // Block below feet
        BlockState bsBody2 = chunk.getBlockState(mutablePos.move(Direction.UP)); // Block at feet level
        BlockState bsHead3; // Block at head level

        while (mutablePos.getY() < topY) {
            bsHead3 = chunk.getBlockState(mutablePos.move(Direction.UP));
            if (bsFeet1.isSolid() && bsBody2.isAir() && bsHead3.isAir()) { // If there is a floor block and space for player body+head
                return OptionalInt.of(mutablePos.getY() - 1);
            }

            bsFeet1 = bsBody2;
            bsBody2 = bsHead3;
        }

        return OptionalInt.empty();
    }

    public static int getChunkHighestNonEmptySectionYOffsetOrTopY(@NotNull Chunk chunk) {
        int i = chunk.getHighestNonEmptySection();
        return i == chunk.getTopYInclusive() ? chunk.getBottomY() : ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(i));
    }

    @Override
    public OptionalInt getY(Chunk chunk, int x, int z) {
        return heightFinder.getY(chunk, x, z);
    }
}
