package mod.fuji.module.initializer.world.manager.structure;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.module.initializer.world.manager.service.WorldService;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executor;

public class RuntimeDimension extends ServerLevel {

    #if MC_VER <= MC_1_20_2
    public RuntimeDimension(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> worldKey, LevelStem dimensionOptions, net.minecraft.server.level.progress.ChunkProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<net.minecraft.world.level.CustomSpawner> spawners, boolean shouldTickTime, @Nullable net.minecraft.world.RandomSequences randomSequencesState) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
    #elif MC_VER > MC_1_20_2 && MC_VER < MC_1_21_9
    public RuntimeDimension(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> worldKey, LevelStem dimensionOptions, net.minecraft.server.level.progress.ChunkProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<net.minecraft.world.level.CustomSpawner> spawners, boolean shouldTickTime, @Nullable net.minecraft.world.RandomSequences randomSequencesState) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
    #elif MC_VER >= MC_1_21_9 && MC_VER < MC_26_1
    public RuntimeDimension(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> worldKey, LevelStem dimensionOptions, boolean debugWorld, long seed, List<net.minecraft.world.level.CustomSpawner> spawners, boolean shouldTickTime, @Nullable net.minecraft.world.RandomSequences randomSequencesState) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
    #elif MC_VER >= MC_26_1
    public RuntimeDimension(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> worldKey, LevelStem dimensionOptions, boolean debugWorld, long seed, List<net.minecraft.world.level.CustomSpawner> spawners, boolean shouldTickTime) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, debugWorld, seed, spawners, shouldTickTime);
    }
    #endif

    private Optional<RuntimeDimensionProperties> getRuntimeDimensionProperties() {
        // NOTE: Other mods may warp the this.properties variable.
        if (this.levelData instanceof RuntimeDimensionProperties runtimeDimensionProperties) {
            return Optional.of(runtimeDimensionProperties);
        }

        return Optional.empty();
    }

    @Override
    public long getSeed() {
        // NOTE: Override the getSeed() method to provide the custom seed before the seed is used by super class.
        String dimensionId = RegistryHelper.getIdAsString(this.dimension());
        Optional<RuntimeDimensionDescriptor> dimensionNode = WorldService.getRuntimeDimensionDescriptor(dimensionId);
        if (dimensionNode.isPresent()) {
            return dimensionNode.get().seed;
        }

        long fallbackSeed = super.getSeed();
        LogUtil.warn("Failed to set the custom seed for dimension {}, we will use the fallback seed {} instead.", dimensionId, fallbackSeed);
        return fallbackSeed;
    }

    @Override
    protected void tickTime() {
        this.getRuntimeDimensionProperties()
            .ifPresentOrElse(runtimeDimensionProperties -> {
            if (!runtimeDimensionProperties.getEffectiveRuntimeDimensionDescriptor().shouldTickTime) {
                return;
            }

            // NOTE: Ignore the step logics for `Time` in `level.dat`, simply mirror it. (Or the scheduled functions will be broken).
            if (shouldDoDayLightCycle(runtimeDimensionProperties)) {
                #if MC_VER < MC_26_1
                runtimeDimensionProperties.setDayTime(runtimeDimensionProperties.getDayTime() + 1L);
                #elif MC_VER >= MC_26_1
                // Since this version, the day time ticking logic is removed from tickTime() method.
                #endif
            }

        }, super::tickTime);
    }

    private static boolean shouldDoDayLightCycle(@NotNull RuntimeDimensionProperties runtimeDimensionProperties) {
        #if MC_VER < MC_1_21_11
        return runtimeDimensionProperties.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DAYLIGHT);
        #elif MC_VER >= MC_1_21_11 && MC_VER < MC_26_1
        return runtimeDimensionProperties.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME);
        #elif MC_VER >= MC_26_1
        return ServerHelper.getServer().getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME);
        #endif

    }


}

