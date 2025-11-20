package mod.fuji.module.initializer.world.manager.structure.util;

#if MC_VER < MC_1_21_9
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoidWorldGenerationProgressListener implements
    #if MC_VER <= MC_1_21_6
    net.minecraft.server.level.progress.ChunkProgressListener
    #elif MC_VER > MC_1_21_6
    net.minecraft.server.WorldGenerationProgressListener
    #endif
{

    public static final VoidWorldGenerationProgressListener INSTANCE = new VoidWorldGenerationProgressListener();

    #if MC_VER <= MC_1_21_6
    @Override
    public void updateSpawnPos(@NotNull net.minecraft.world.level.ChunkPos chunkPos) {}
    #elif MC_VER > MC_1_21_6
    @Override
    public void start(net.minecraft.util.math.ChunkPos spawnPos) {}
    #endif


    #if MC_VER <= MC_1_21_6
    @Override
    public void onStatusChange(@NotNull net.minecraft.world.level.ChunkPos chunkPos, @Nullable net.minecraft.world.level.chunk.status.ChunkStatus chunkStatus) {}
    #elif MC_VER > MC_1_21_6
    @Override
    public void setChunkStatus(net.minecraft.util.math.ChunkPos pos, @Nullable net.minecraft.world.chunk.ChunkStatus status) {}
    #endif


    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }
}
#elif MC_VER >= MC_1_21_9
@Deprecated
public class VoidWorldGenerationProgressListener {}
#endif

