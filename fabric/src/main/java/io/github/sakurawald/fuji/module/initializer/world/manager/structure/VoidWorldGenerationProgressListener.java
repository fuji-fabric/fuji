package io.github.sakurawald.fuji.module.initializer.world.manager.structure;

import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class VoidWorldGenerationProgressListener implements WorldGenerationProgressListener {

    public static final VoidWorldGenerationProgressListener INSTANCE = new VoidWorldGenerationProgressListener();

    @Override
    public void start(ChunkPos spawnPos) {
        // no-op
    }

    @Override
    public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {
        // no-op
    }

    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }
}
