package io.github.sakurawald.fuji.module.initializer.world.service;

import com.google.common.collect.ImmutableList;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.event.impl.ServerTickEvents;
import io.github.sakurawald.fuji.core.event.impl.ServerWorldEvents;
import io.github.sakurawald.fuji.core.extension.SimpleRegistryExtension;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.module.initializer.world.WorldInitializer;
import io.github.sakurawald.fuji.module.initializer.world.accessor.ExtendedDimensionOptions;
import io.github.sakurawald.fuji.module.initializer.world.structure.RuntimeDimensionDescriptor;
import io.github.sakurawald.fuji.module.initializer.world.structure.RuntimeDimension;
import io.github.sakurawald.fuji.module.initializer.world.structure.RuntimeDimensionProperties;
import io.github.sakurawald.fuji.module.initializer.world.structure.VoidWorldGenerationProgressListener;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SimpleRegistry;

#if MC_VER <= MC_1_20_4
import com.mojang.serialization.Lifecycle;
#elif MC_VER > MC_1_20_4
import net.minecraft.registry.entry.RegistryEntryInfo;
#endif

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldService {

    private static final Set<ServerWorld> dimensionDeletionQueue = new ReferenceOpenHashSet<>();
    private static final Set<RuntimeDimensionDescriptor> dimensionCreationQueue = new ReferenceOpenHashSet<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> processDimensionCreationAndDeletionQueue());
    }

    private static void processDimensionCreationAndDeletionQueue() {
        dimensionDeletionQueue.removeIf(WorldService::tryDeleteDimension);
        dimensionCreationQueue.removeIf(WorldService::tryCreateDimension);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void requestToDeleteDimension(@NotNull ServerWorld world) {
        ServerHelper.getServer().submit(() -> {
            dimensionDeletionQueue.add(world);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void requestToCreateDimension(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        ServerHelper.getServer().submit(() -> {
            dimensionCreationQueue.add(runtimeDimensionDescriptor);
        });
    }

    private static boolean tryDeleteDimension(@NotNull ServerWorld world) {
        if (world.getPlayers().isEmpty()) {
            deleteDimension(world);
            return true;
        } else {
            evacuatePlayers(world);
            return false;
        }
    }


    private static boolean tryCreateDimension(@NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        // NOTE: Wait the target dimension to be deleted first (If there is a deletion ticket for it).
        if (dimensionDeletionQueue.stream().anyMatch(it -> RegistryHelper.toString(it).equals(runtimeDimensionDescriptor.dimension))) {
            return false;
        }

        createDimension(runtimeDimensionDescriptor);
        return true;
    }

    @SuppressWarnings("deprecation")
    private static void createDimension(@NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        MinecraftServer server = ServerHelper.getServer();
        Identifier dimensionIdentifier = RegistryHelper.makeIdentifier(runtimeDimensionDescriptor.dimension);
        Identifier dimensionTypeIdentifier = RegistryHelper.makeIdentifier(runtimeDimensionDescriptor.dimension_type);

        try {
            /* Make the dimension properties. */
            // note: we use the same WorldData from OVERWORLD
            RuntimeDimensionProperties worldProperties = new RuntimeDimensionProperties(server.getSaveProperties(), runtimeDimensionDescriptor);

            /* Make the dimension options. */
            @Nullable DimensionOptions dimensionOptions = makeDimensionOptions(dimensionTypeIdentifier);
            ((ExtendedDimensionOptions) (Object) dimensionOptions).fuji$setSaveDimensionOptions(false);

            /* Make the dimension instance. */
            RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, dimensionIdentifier);
            LogUtil.debug("Make instance of world with registry key of type `World`: {}", worldRegistryKey);
            ServerWorld dimension;

            dimension = new RuntimeDimension(server,
                Util.getMainWorkerExecutor(),
                server.session,
                worldProperties,
                worldRegistryKey,
                dimensionOptions,
                VoidWorldGenerationProgressListener.INSTANCE,
                false,
                BiomeAccess.hashSeed(runtimeDimensionDescriptor.seed),
                ImmutableList.of(),
                runtimeDimensionDescriptor.shouldTickTime,
                null);
        // start dragon fight if the dimension type is the end.
        if (dimensionTypeIdentifier.equals(DimensionTypes.THE_END_ID)) {
            dimension.setEnderDragonFight(new EnderDragonFight(dimension, dimension.getSeed(), EnderDragonFight.Data.DEFAULT));
        }

        /* Register the dimension. */
        SimpleRegistry<DimensionOptions> dimensionOptionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
        boolean original = ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$isFrozen();
        ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$setFrozen(false);

        RegistryKey<DimensionOptions> dimensionOptionsRegistryKey = RegistryKeys.toDimensionKey(worldRegistryKey);

        if (!dimensionOptionsRegistry.contains(dimensionOptionsRegistryKey)) {
            LogUtil.debug("Add entry for dimension options registry: key = {}, value = {}", dimensionOptionsRegistryKey, dimensionOptions);
            #if MC_VER <= MC_1_20_4
            dimensionOptionsRegistry.add(dimensionOptionsRegistryKey, dimensionOptions, Lifecycle.stable());
            #elif MC_VER > MC_1_20_4
            dimensionOptionsRegistry.add(dimensionOptionsRegistryKey, dimensionOptions, RegistryEntryInfo.DEFAULT);
            #endif
        }
        ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$setFrozen(original);

        server.worlds.put(dimension.getRegistryKey(), dimension);
        ServerWorldEvents.LOAD.invoker().fire(server, dimension);

        /* Start ticking it. */
        dimension.tick(() -> true);

        } catch (Exception e) {
            LogUtil.error("Failed to make ServerWorld instance: dimensionId = {}, dimensionTypeId = {}", dimensionIdentifier, dimensionTypeIdentifier, e);
        }
    }

    private static void evacuatePlayers(@NotNull ServerWorld dimension) {
        ServerWorld safeDimension = dimension.getServer().getOverworld();
        BlockPos safeBlockPos = safeDimension.getSpawnPos();

        List<ServerPlayerEntity> players = new ArrayList<>(dimension.getPlayers());
        for (ServerPlayerEntity player : players) {
            GlobalPos from = GlobalPos.of(player);
            GlobalPos to = new GlobalPos(safeDimension, safeBlockPos.getX() + 0.5, safeBlockPos.getY() + 0.5, safeBlockPos.getZ() + 0.5, 0, 0);
            TeleportTicket teleportTicket = TeleportTicket.makeVipTicket(player, from, to);

            Managers.getBossBarManager().addTicket(teleportTicket);
        }
    }

    private static void deleteDimension(@NotNull ServerWorld world) {
        MinecraftServer server = world.getServer();

        // FIXME: Use the vanilla function to handle dimension shutdown.
        RegistryKey<World> dimensionKey = world.getRegistryKey();
        if (server.worlds.remove(dimensionKey, world)) {
            /* Fire an unload event */
            ServerWorldEvents.UNLOAD.invoker().fire(server, world);

            /* remove the entry from registry */
            SimpleRegistry<DimensionOptions> dimensionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
            SimpleRegistryExtension.remove(dimensionsRegistry, dimensionKey.getValue());

            /* Delete world files. */
            File worldDirectory = server.session.getWorldDirectory(dimensionKey).toFile();
            deleteFiles(worldDirectory);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteFiles(@NotNull File file) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File child : files) {
                if (child.isDirectory()) {
                    deleteFiles(child);
                } else {
                    child.delete();
                }
            }
        }
    }

    private static @Nullable DimensionOptions makeDimensionOptions(Identifier dimensionTypeIdentifier) {
        /* Get an existing dimension options from registry. */
        RegistryEntry.Reference<DimensionType> dimensionTypeEntry = getDimensionTypeEntry(dimensionTypeIdentifier);
        if (dimensionTypeEntry == null) return null;

        // NOTE: Clone a DimensionOptions instance from existing one.
        ChunkGenerator chunkGenerator = getChunkGenerator(dimensionTypeIdentifier);
        if (chunkGenerator == null) return null;
        return new DimensionOptions(dimensionTypeEntry, chunkGenerator);
    }

    private static @Nullable ChunkGenerator getChunkGenerator(Identifier dimensionTypeIdentifier) {
        Registry<DimensionOptions> dimensionRegistry = RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);

        @Nullable DimensionOptions originalDimensionOptions = dimensionRegistry.get(dimensionTypeIdentifier);
        if (originalDimensionOptions == null) {
            return null;
        }

        ChunkGenerator chunkGenerator = originalDimensionOptions.chunkGenerator();
        return chunkGenerator;
    }

    private static RegistryEntry.@Nullable Reference<DimensionType> getDimensionTypeEntry(Identifier dimensionTypeIdentifier) {
        Optional<RegistryEntry.Reference<DimensionType>> dimensionTypeReference = RegistryHelper.ofRegistryEntry(RegistryKeys.DIMENSION_TYPE, dimensionTypeIdentifier);
        if (dimensionTypeReference.isEmpty()) {
            LogUtil.error("Failed to make DimensionOptions: The DimensionTypeEntry {} is null.", dimensionTypeIdentifier);
            return null;
        }
        return dimensionTypeReference.get();
    }

    private static @NotNull FlatChunkGenerator getFlatChunkGenerator(RegistryEntry<DimensionType> dimensionTypeEntry, ChunkGenerator chunkGenerator) {
        DimensionOptions dimensionOptions = new DimensionOptions(dimensionTypeEntry, chunkGenerator);
        DynamicRegistryManager.Immutable dynamicRegistryManager = ServerHelper.getServer().getCombinedDynamicRegistries().getCombinedRegistryManager();
        RegistryWrapper.Impl<Biome> registryEntryLookup = dynamicRegistryManager.getOrThrow(RegistryKeys.BIOME);
        RegistryWrapper.Impl<StructureSet> registryEntryLookup2 = dynamicRegistryManager.getOrThrow(RegistryKeys.STRUCTURE_SET);
        RegistryWrapper.Impl<PlacedFeature> registryEntryLookup3 = dynamicRegistryManager.getOrThrow(RegistryKeys.PLACED_FEATURE);
        FlatChunkGeneratorConfig flatChunkGeneratorConfig = FlatChunkGeneratorConfig.getDefaultConfig(registryEntryLookup, registryEntryLookup2, registryEntryLookup3);
        FlatChunkGenerator flatChunkGenerator = new FlatChunkGenerator(flatChunkGeneratorConfig);
        return flatChunkGenerator;
    }

    public static boolean existsDimension(Identifier dimensionId) {
        boolean dimensionExistedInRuntime = ServerHelper
            .getWorlds()
            .stream()
            .anyMatch(it -> RegistryHelper.toString(it).equals(dimensionId.toString()));
        boolean dimensionExistedInConfig = getDimensionDescriptor(dimensionId.toString()).isPresent();

        return dimensionExistedInRuntime || dimensionExistedInConfig;
    }

    public static void deleteDimensionNode(String dimensionId) {
        Optional<RuntimeDimensionDescriptor> first = WorldInitializer.world.model().dimension_list.stream().filter(o -> o.getDimension().equals(dimensionId)).findFirst();
        first.ifPresent(dimensionNode -> {
            WorldInitializer.world.model().dimension_list.remove(dimensionNode);
            WorldInitializer.world.writeStorage();
        });
    }

    public static void saveRuntimeWorldConfigs() {
        WorldInitializer.config.writeStorage();
    }

    public static Optional<RuntimeDimensionDescriptor> getDimensionDescriptor(String dimensionId) {
        return WorldInitializer.world.model()
            .dimension_list
            .stream()
            .filter(it -> it.dimension.equalsIgnoreCase(dimensionId))
            .findFirst();
    }

    public static List<RuntimeDimensionDescriptor> getUnloadedDimensionDescriptors() {
        return WorldInitializer.world.model().dimension_list
            .stream()
            .filter(it -> !it.isDimensionLoaded())
            .toList();

    }
}


