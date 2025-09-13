package io.github.sakurawald.fuji.core.service.random_teleport;

import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

public class ChunkCandidateBlocksGenerator {

    public static @NotNull Iterable<BlockPos.Mutable> getChunkCandidateBlocks(@NotNull ChunkPos chunkPos) {
        return () -> new Iterator<>() {
            private final BlockPos.Mutable currentValue = new BlockPos.Mutable();
            private int i = -1;

            @Override
            public boolean hasNext() {
                return i < 4;
            }

            @Override
            public BlockPos.Mutable next() {
                i++;
                return switch (i) {
                    case 0 -> currentValue.set(chunkPos.getStartX(), 0, chunkPos.getStartZ());
                    case 1 -> currentValue.set(chunkPos.getStartX(), 0, chunkPos.getEndZ());
                    case 2 -> currentValue.set(chunkPos.getEndX(), 0, chunkPos.getStartZ());
                    case 3 -> currentValue.set(chunkPos.getEndX(), 0, chunkPos.getEndZ());
                    case 4 -> currentValue.set(chunkPos.getCenterX(), 0, chunkPos.getCenterZ());
                    default -> throw new IllegalStateException("Unexpected value: " + i);
                };
            }
        };
    }
}
