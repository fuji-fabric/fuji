package io.github.sakurawald.fuji.core.service.async_chunk_loader;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.manager.impl.task.structure.GameTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Getter;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;

public class AsyncChunkLoadTask extends GameTask {

    @Getter
    CompletableFuture<Void> chunkAsyncLoadingFuture = new CompletableFuture<>();
    final AtomicBoolean consumed = new AtomicBoolean(false);

    public AsyncChunkLoadTask(@NotNull ServerWorld serverWorld, @NotNull ChunkPos chunkPos, int timeoutTicks, @NotNull Consumer<Chunk> chunkConsumer, @NotNull Runnable onFailed) {
        super(timeoutTicks);

        /* Set the onTick() function. */
        this.setOnTick(() -> {
            /* Return if consumed. */
            if (consumed.get() || chunkAsyncLoadingFuture.isDone()) {
                return;
            }

            /* Make the chunk future. */
            CompletableFuture<OptionalChunk<Chunk>> chunkFuture = serverWorld.getChunkManager().getChunkFuture(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);

            /* Try to consume it. */
            @Unused CompletableFuture<Void> unused = chunkFuture
                .thenAccept(it -> {
                    it.ifPresent(chunk -> {
                        if (consumed.compareAndSet(false, true)) {
                            this.setCompleted(true);
                            chunkConsumer.accept(chunk);
                            chunkAsyncLoadingFuture.complete(null);
                        }
                    });
                })
                .exceptionally(ex -> {
                    chunkAsyncLoadingFuture.completeExceptionally(ex);
                    return null;
                });
        });

        /* Set the onEnd() function. */
        this.setOnEnd(() -> {
            if (!consumed.get()) {
                onFailed.run();
            }
            chunkAsyncLoadingFuture.complete(null);
        });
    }

}
