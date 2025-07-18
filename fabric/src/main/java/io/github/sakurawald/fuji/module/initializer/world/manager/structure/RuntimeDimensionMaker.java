package io.github.sakurawald.fuji.module.initializer.world.manager.structure;

import com.google.common.collect.ImmutableList;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.structure.Pair;
import io.github.sakurawald.fuji.module.initializer.world.manager.accessor.ExtendedDimensionOptions;
import io.github.sakurawald.fuji.module.initializer.world.manager.command.argument.wrapper.ChunkGeneratorType;
import io.github.sakurawald.fuji.module.initializer.world.manager.service.FlatPresetParser;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RuntimeDimensionMaker {

    public static Pair<ServerWorld, DimensionOptions> makeRuntimeDimension(@NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        MinecraftServer server = ServerHelper.getServer();
        Identifier dimensionIdentifier = RegistryHelper.makeIdentifier(runtimeDimensionDescriptor.dimension);
        Identifier dimensionTypeIdentifier = RegistryHelper.makeIdentifier(runtimeDimensionDescriptor.dimension_type);

        try {
            /* Make the dimension properties. */
            // note: we use the same WorldData from OVERWORLD
            RuntimeDimensionProperties worldProperties = new RuntimeDimensionProperties(server.getSaveProperties(), runtimeDimensionDescriptor);

            /* Make the dimension options. */
            @Nullable DimensionOptions dimensionOptions = makeDimensionOptions(runtimeDimensionDescriptor);
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

            return new Pair<>(dimension, dimensionOptions);
        } catch (Exception e) {
            throw e;
        }

    }

    private static @NotNull DimensionOptions makeDimensionOptions(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        if (runtimeDimensionDescriptor.worldPresetType != null) {
            return makeDimensionOptionsWithWorldPreset(runtimeDimensionDescriptor);
        } else {
            return makeDimensionOptionsWithCustomization(runtimeDimensionDescriptor);
        }
    }

    private static @NotNull DimensionOptions makeDimensionOptionsWithWorldPreset(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        Registry<WorldPreset> worldPresetRegistry = RegistryHelper.ofRegistry(RegistryKeys.WORLD_PRESET);
        assert runtimeDimensionDescriptor.worldPresetType != null;
        RegistryKey<WorldPreset> worldPresetKey = runtimeDimensionDescriptor.worldPresetType.toWorldPresetKey();
        WorldPreset worldPreset = worldPresetRegistry.get(worldPresetKey);
        if (worldPreset == null) {
            throw new RuntimeException("Failed to make DimensionOptions for dimension %s, the WorldPreset didn't existed in the registry.".formatted(runtimeDimensionDescriptor.dimension));
        }

        Optional<DimensionOptions> overworldDimensionOptionsOptional = worldPreset.getOverworld();
        if (overworldDimensionOptionsOptional.isEmpty()) {
            throw new RuntimeException("Failed to make DimensionOptions for dimension %s, the WorldPreset#getOverworld() returns null.".formatted(runtimeDimensionDescriptor.dimension));
        }

        return overworldDimensionOptionsOptional.get();
    }

    private static @NotNull DimensionOptions makeDimensionOptionsWithCustomization(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        /* Get an existing dimension options from registry. */
        RegistryEntry<DimensionType> dimensionTypeEntry = getDimensionTypeEntry(runtimeDimensionDescriptor);
        ChunkGenerator chunkGenerator = makeChunkGenerator(runtimeDimensionDescriptor);

        // NOTE: Make a new DimensionOptions instance. (One DimensionOptions instance can only be used by a ServerWorld instance)
        return new DimensionOptions(dimensionTypeEntry, chunkGenerator);
    }

    private static @NotNull ChunkGenerator makeChunkGenerator(RuntimeDimensionDescriptor dimensionDescriptor) {
        if (dimensionDescriptor.chunkGeneratorType == ChunkGeneratorType.NOISE) {
            return makeNoiseChunkGenerator(dimensionDescriptor);
        }

        if (dimensionDescriptor.chunkGeneratorType == ChunkGeneratorType.FLAT) {
            return makeFlatChunkGenerator(dimensionDescriptor);
        }

        throw new RuntimeException("Failed to make the chunk generator for dimension %s.".formatted(dimensionDescriptor.dimension));
    }

    private static @NotNull ChunkGenerator makeNoiseChunkGenerator(RuntimeDimensionDescriptor dimensionDescriptor) {
        Registry<DimensionOptions> dimensionRegistry = RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
        Identifier dimensionTypeIdentifier = RegistryHelper.makeIdentifier(dimensionDescriptor.dimension_type);
        @Nullable DimensionOptions existedDimensionOptions = dimensionRegistry.get(dimensionTypeIdentifier);
        if (existedDimensionOptions == null) {
            throw new RuntimeException("Failed to make chunk generator, there is no existed DimensionOptions for dimension type %s.".formatted(dimensionTypeIdentifier));
        }

        // NOTE: Copy the existed chunk generator, to ensure the settings of chunk generator is identical.
        return existedDimensionOptions.chunkGenerator();
    }

    private static @NotNull RegistryEntry<DimensionType> getDimensionTypeEntry(RuntimeDimensionDescriptor dimensionDescriptor) {
        Identifier dimensionTypeIdentifier = RegistryHelper.makeIdentifier(dimensionDescriptor.dimension_type);
        Optional<RegistryEntry<DimensionType>> dimensionTypeEntry = RegistryHelper.ofRegistryEntry(RegistryKeys.DIMENSION_TYPE, dimensionTypeIdentifier);
        if (dimensionTypeEntry.isEmpty()) {
            throw new RuntimeException("Failed to make DimensionOptions: The DimensionTypeEntry %s is null.".formatted(dimensionTypeIdentifier));
        }
        return dimensionTypeEntry.get();
    }

    private static @NotNull FlatChunkGenerator makeFlatChunkGenerator(RuntimeDimensionDescriptor dimensionDescriptor) {
        /* Make the default flat chunk generator config. */
        RegistryEntryLookup<Biome> biomeLookup = RegistryHelper.ofRegistryWrapper(RegistryKeys.BIOME);
        RegistryEntryLookup<StructureSet> structureSetLookup = RegistryHelper.ofRegistryWrapper(RegistryKeys.STRUCTURE_SET);
        RegistryEntryLookup<PlacedFeature> placedFeatureLookup = RegistryHelper.ofRegistryWrapper(RegistryKeys.PLACED_FEATURE);
        FlatChunkGeneratorConfig flatChunkGeneratorConfig = FlatChunkGeneratorConfig.getDefaultConfig(biomeLookup, structureSetLookup, placedFeatureLookup);

        /* Make the parsed flat chunk generator config. */
        String presetString = dimensionDescriptor.chunkGeneratorParameters;
        if (presetString != null && !presetString.isBlank()) {
            RegistryEntryLookup<Block> blockLookup = RegistryHelper.ofRegistryWrapper(RegistryKeys.BLOCK);
            flatChunkGeneratorConfig = FlatPresetParser.parsePresetString(blockLookup, biomeLookup, structureSetLookup, placedFeatureLookup, presetString, flatChunkGeneratorConfig);
        }

        return new FlatChunkGenerator(flatChunkGeneratorConfig);
    }
}
