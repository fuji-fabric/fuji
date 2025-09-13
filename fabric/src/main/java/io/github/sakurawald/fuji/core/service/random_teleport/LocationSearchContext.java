package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

@Data
public class LocationSearchContext {

    @NotNull RandomTeleportSettings settings;
    @NotNull List<ChunkPos> chunkPosQueue;
    @NotNull List<BlockPos> blockPosQueue;
    int attempts = 0;
    @NotNull Optional<BlockPos> result;

    boolean hasRemainingAttempts() {
        return getAttempts() < getMaxAttempts();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public int getMaxAttempts() {
        return this.settings.getMaxTryTimes();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull LocationSearchContext of(@NotNull RandomTeleportSettings settings) {
        LocationSearchContext locationSearchContext = new LocationSearchContext(settings, new ArrayList<>(), new ArrayList<>(), Optional.empty());
        return locationSearchContext;
    }

}
