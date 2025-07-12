package io.github.sakurawald.fuji.module.initializer.world.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.world.service.WorldService;
import java.util.Optional;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
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

public class RuntimeWorld extends ServerWorld {

    #if MC_VER <= MC_1_20_2
    public RuntimeWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
    #elif MC_VER > MC_1_20_2
    public RuntimeWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
    #endif

    private Optional<RuntimeWorldProperties> getRuntimeWorldProperties() {
        if (this.properties instanceof RuntimeWorldProperties runtimeWorldProperties) {
            return Optional.of(runtimeWorldProperties);
        }

        // NOTE: Other mods warp the this.properties variable, makes ours assumption failed.
        return Optional.empty();
    }

    @Override
    public long getSeed() {
        // NOTE: Override the getSeed() method to provide the custom seed before the seed is used by super class.
        String dimensionId = RegistryHelper.toString(this.getRegistryKey());
        Optional<DimensionNode> dimensionNode = WorldService.getDimensionNode(dimensionId);
        if (dimensionNode.isPresent()) {
            return dimensionNode.get().seed;
        }

        long fallbackSeed = super.getSeed();
        LogUtil.warn("Failed to set the custom seed for dimension {}, we will use the fallback seed {} instead.", dimensionId, fallbackSeed);
        return fallbackSeed;
    }

    @Override
    protected void tickTime() {
        // Tick the time, but should not set the ServerWorld.worldProperties, or it may break some datapacks in scheduled functions.
         this.setTimeOfDay(this.properties.getTimeOfDay() + 1L);
    }

    @Override
    public GameRules getGameRules() {
        // NOTE: For `keepInventory` game rule. Its value is checked in copyFrom() method after the player is dead. The value comes from the re-spawn dimension's world properties.

        Optional<RuntimeWorldProperties> opt = getRuntimeWorldProperties();
        return opt
            .map(RuntimeWorldProperties::getGameRules)
            .orElseGet(super::getGameRules);
    }


}

