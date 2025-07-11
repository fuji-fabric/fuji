package io.github.sakurawald.fuji.module.initializer.world.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
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

    /*
        The main issue is that the runtime world must return the custom seed through World#getSeed, including within the ServerWorld constructor.
        The solution is to override World#getSeed in a way that the seed is initialized before it is called.
        Please note that: all the resource world will not save its data (properties) into level.dat, so if you restart the server.
        then the seed of resource world will be changed randomly (and then the chunk generator will generate new chunks with the new seed).
     */
    @Override
    public long getSeed() {
        return getRuntimeWorldProperties().dimensionNode.seed;
    }

    private RuntimeWorldProperties getRuntimeWorldProperties() {
        return (RuntimeWorldProperties) this.properties;
    }

    @Override
    protected void tickTime() {
        // Tick the time, but should not set the ServerWorld.worldProperties, or it may break some datapacks in scheduled functions.
         this.setTimeOfDay(this.properties.getTimeOfDay() + 1L);
    }

    @Override
    public GameRules getGameRules() {
        // NOTE: For `keepInventory` game rule. Its value is checked in copyFrom() method after the player is dead. The value comes from the re-spawn dimension's world properties.
        return getRuntimeWorldProperties().getGameRules();
    }


}

