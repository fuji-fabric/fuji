package io.github.sakurawald.fuji.core.service.async_chunk_loader;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.manager.impl.task.structure.GameTask;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Getter;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;

public class AsyncChunkLoadTask extends GameTask {

    @Getter
    final CompletableFuture<Void> chunkAsyncLoadingFuture = new CompletableFuture<>();
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
            CompletableFuture<Optional<Chunk>> chunkFuture = getChunkFuture(serverWorld, chunkPos);

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

    private static CompletableFuture<Optional<Chunk>> getChunkFuture(@NotNull ServerWorld serverWorld, @NotNull ChunkPos chunkPos) {
        var future = serverWorld.getChunkManager().getChunkFuture(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);

        #if MC_VER <= MC_1_20_4
        return future.thenApply(it -> it.left());
        #elif MC_VER > MC_1_20_4
        return future.thenApply(it -> Optional.ofNullable(it.orElse(null)));
        #endif
    }

}
