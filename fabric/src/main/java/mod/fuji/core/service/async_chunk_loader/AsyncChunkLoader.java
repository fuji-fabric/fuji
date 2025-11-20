package mod.fuji.core.service.async_chunk_loader;

import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.service.game_task.GameTaskManager;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;

@TestCase(action = "Test the functionality of async chunk loading.", targets = {
    "The RTP process should not block the game-playing. (Ticking entities, selecting target blocks...)",
    "Throwing item entities during RTP process, the game should be ticked normally.",
    "Start 3 RTP processes at the same time, it should be processed normally.",
    "Run `/execute as @a run rtp` command, it should be processed normally."
})
public class AsyncChunkLoader {

    public static @NotNull CompletableFuture<Void> loadChunkAsync(@NotNull ServerLevel serverWorld, @NotNull ChunkPos chunkPos, int timeoutTicks, @NotNull Consumer<ChunkAccess> chunkConsumer, @NotNull Runnable onFailed) {
        AsyncChunkLoadTask task = new AsyncChunkLoadTask(serverWorld, chunkPos, timeoutTicks, chunkConsumer, onFailed);
        GameTaskManager.submitTask(task);
        return task.getChunkAsyncLoadingFuture();
    }

}
