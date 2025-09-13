package io.github.sakurawald.fuji.core.service.random_teleport;

import java.util.Optional;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class PositionYDownTopSearcher extends PositionYSearcher {
    @Override
    @NotNull
    public Optional<Integer> search(@NotNull Chunk chunk, int blockPosX, int blockPosZ) {
        return PositionYSearcher.search(chunk, blockPosX, blockPosZ, Direction.UP);
    }
}
