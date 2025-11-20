package mod.fuji.core.service.random_teleport.generator;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

public class ChunkCandidateBlocksGenerator {

    public static @NotNull Iterable<BlockPos.MutableBlockPos> getChunkCandidateBlocks(@NotNull ChunkPos chunkPos) {
        return () -> new Iterator<>() {
            private final BlockPos.MutableBlockPos currentValue = new BlockPos.MutableBlockPos();
            private int i = -1;

            @Override
            public boolean hasNext() {
                return i < 4;
            }

            @Override
            public BlockPos.MutableBlockPos next() {
                i++;
                return switch (i) {
                    case 0 -> currentValue.set(chunkPos.getMiddleBlockX(), 0, chunkPos.getMiddleBlockZ());
                    case 1 -> currentValue.set(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
                    case 2 -> currentValue.set(chunkPos.getMinBlockX(), 0, chunkPos.getMaxBlockZ());
                    case 3 -> currentValue.set(chunkPos.getMaxBlockX(), 0, chunkPos.getMinBlockZ());
                    case 4 -> currentValue.set(chunkPos.getMaxBlockX(), 0, chunkPos.getMaxBlockZ());
                    default -> throw new IllegalStateException("Unexpected value: " + i);
                };
            }
        };
    }
}
