package mod.fuji.core.service.random_teleport.searcher;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;

public class PositionYTopDownSearcher extends PositionYSearcher {
    @Override
    @NotNull
    public Optional<Integer> search(@NotNull ChunkAccess chunk, int blockPosX, int blockPosZ) {
        return PositionYSearcher.search(chunk, blockPosX,blockPosZ, Direction.DOWN);
    }
}
