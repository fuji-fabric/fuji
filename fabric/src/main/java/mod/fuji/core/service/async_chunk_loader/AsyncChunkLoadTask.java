package mod.fuji.core.service.async_chunk_loader;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.service.game_task.structure.GameTask;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.NotNull;

public class AsyncChunkLoadTask extends GameTask {

    @Getter
    final CompletableFuture<Void> chunkAsyncLoadingFuture = new CompletableFuture<>();
    final AtomicBoolean consumed = new AtomicBoolean(false);

    public AsyncChunkLoadTask(@NotNull ServerLevel serverWorld, @NotNull ChunkPos chunkPos, int timeoutTicks, @NotNull Consumer<ChunkAccess> chunkConsumer, @NotNull Runnable onFailed) {
        super(timeoutTicks);

        /* Set the onTick() function. */
        this.setOnTick(() -> {
            /* Return if consumed. */
            if (consumed.get() || chunkAsyncLoadingFuture.isDone()) {
                return;
            }

            /* Make the chunk future. */
            CompletableFuture<Optional<ChunkAccess>> chunkFuture = getChunkFuture(serverWorld, chunkPos);

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

    private static CompletableFuture<Optional<ChunkAccess>> getChunkFuture(@NotNull ServerLevel serverWorld, @NotNull ChunkPos chunkPos) {
        var future = serverWorld.getChunkSource().getChunkFutureMainThread(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);

        #if MC_VER <= MC_1_20_4
        return future.thenApply(it -> it.left());
        #elif MC_VER > MC_1_20_4
        return future.thenApply(it -> Optional.ofNullable(it.orElse(null)));
        #endif
    }

}
