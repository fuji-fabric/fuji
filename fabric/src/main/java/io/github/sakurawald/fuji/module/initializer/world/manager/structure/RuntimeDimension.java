package io.github.sakurawald.fuji.module.initializer.world.manager.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.world.manager.service.WorldService;
import java.util.Optional;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
#if MC_VER <= MC_1_20_2
import net.minecraft.world.spawner.Spawner;
#elif MC_VER > MC_1_20_2
import net.minecraft.world.spawner.SpecialSpawner;
#endif
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;

public class RuntimeDimension extends ServerWorld {

    #if MC_VER <= MC_1_20_2
    public RuntimeDimension(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
    #elif MC_VER > MC_1_20_2
    public RuntimeDimension(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
    #endif

    private Optional<RuntimeDimensionProperties> getRuntimeDimensionProperties() {
        // NOTE: Other mods may warp the this.properties variable.
        if (this.properties instanceof RuntimeDimensionProperties runtimeDimensionProperties) {
            return Optional.of(runtimeDimensionProperties);
        }

        return Optional.empty();
    }

    @Override
    public long getSeed() {
        // NOTE: Override the getSeed() method to provide the custom seed before the seed is used by super class.
        String dimensionId = RegistryHelper.toIdString(this.getRegistryKey());
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
            if (runtimeDimensionProperties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                runtimeDimensionProperties.setTimeOfDay(runtimeDimensionProperties.getTimeOfDay() + 1L);
            }

        }, super::tickTime);
    }

}

