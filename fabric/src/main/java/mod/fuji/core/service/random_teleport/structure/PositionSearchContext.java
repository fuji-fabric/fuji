package mod.fuji.core.service.random_teleport.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

@Data
public class PositionSearchContext {

    @NotNull ServerPlayer player;
    @NotNull RandomTeleportSettings settings;
    @NotNull List<ChunkPos> chunkPosQueue;
    @NotNull List<BlockPos> blockPosQueue;
    int attempts = 0;
    @NotNull Optional<BlockPos> result;

    public boolean hasRemainingAttempts() {
        return getAttempts() < getMaxAttempts();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public int getMaxAttempts() {
        return this.settings.getMaxTryTimes();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull PositionSearchContext of(@NotNull ServerPlayer player, @NotNull RandomTeleportSettings settings) {
        PositionSearchContext positionSearchContext = new PositionSearchContext(player, settings, new ArrayList<>(), new ArrayList<>(), Optional.empty());
        return positionSearchContext;
    }

}
