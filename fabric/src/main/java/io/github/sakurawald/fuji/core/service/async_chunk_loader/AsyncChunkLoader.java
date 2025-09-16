package io.github.sakurawald.fuji.core.service.async_chunk_loader;

import io.github.sakurawald.fuji.core.manager.impl.task.GameTaskManager;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class AsyncChunkLoader {

    public static @NotNull CompletableFuture<Void> loadChunkAsync(@NotNull ServerWorld serverWorld, @NotNull ChunkPos chunkPos, int timeoutTicks, @NotNull Consumer<Chunk> chunkConsumer, @NotNull Runnable onFailed) {
        AsyncChunkLoadTask task = new AsyncChunkLoadTask(serverWorld, chunkPos, timeoutTicks, chunkConsumer, onFailed);
        GameTaskManager.submitTask(task);
        return task.getChunkAsyncLoadingFuture();
    }

}
