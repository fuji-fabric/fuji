package io.github.sakurawald.fuji.core.service.async_chunk_loader;

import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.manager.impl.task.GameTaskManager;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

@TestCase(action = "Test the functionality of async chunk loading.", targets = {
    "The RTP process should not block the game-playing. (Ticking entities, selecting target blocks...)",
    "Throwing item entities during RTP process, the game should be ticked normally.",
    "Start 3 RTP processes at the same time, it should be processed normally.",
    "Run `/execute as @a run rtp` command, it should be processed normally."
})
public class AsyncChunkLoader {

    public static @NotNull CompletableFuture<Void> loadChunkAsync(@NotNull ServerWorld serverWorld, @NotNull ChunkPos chunkPos, int timeoutTicks, @NotNull Consumer<Chunk> chunkConsumer, @NotNull Runnable onFailed) {
        AsyncChunkLoadTask task = new AsyncChunkLoadTask(serverWorld, chunkPos, timeoutTicks, chunkConsumer, onFailed);
        GameTaskManager.submitTask(task);
        return task.getChunkAsyncLoadingFuture();
    }

}
