package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class LocationSearchContext {

    @NotNull RandomTeleportSettings settings;
    @NotNull List<ChunkPos> chunkPosQueue;
    @NotNull List<BlockPos> blockPosQueue;
    int attempts = 0;

    public void incrementAttempts() {
        this.attempts++;
    }

    public static @NotNull LocationSearchContext of(@NotNull RandomTeleportSettings settings) {
        LocationSearchContext locationSearchContext = new LocationSearchContext();
        locationSearchContext.settings = settings;
        locationSearchContext.chunkPosQueue = new ArrayList<>();
        locationSearchContext.blockPosQueue = new ArrayList<>();
        return locationSearchContext;
    }

}
