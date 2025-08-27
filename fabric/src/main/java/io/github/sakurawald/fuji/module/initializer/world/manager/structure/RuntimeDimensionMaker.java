package io.github.sakurawald.fuji.module.initializer.world.manager.structure;

import com.google.common.collect.ImmutableList;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.structure.Pair;
import io.github.sakurawald.fuji.module.initializer.world.manager.accessor.ExtendedDimensionOptions;
import io.github.sakurawald.fuji.module.initializer.world.manager.command.argument.wrapper.ChunkGeneratorType;
import io.github.sakurawald.fuji.module.initializer.world.manager.service.FlatPresetParser;
import io.github.sakurawald.fuji.module.initializer.world.manager.structure.util.VoidWorldGenerationProgressListener;
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
import net.minecraft.util.math.random.RandomSequencesState;
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

        /* Make the dimension properties. */
        // NOTE: Here we just mirror the `SaveProperties` from `minecraft:overworld`.
        RuntimeDimensionProperties worldProperties = new RuntimeDimensionProperties(server.getSaveProperties(), runtimeDimensionDescriptor);

        /* Make the dimension options. */
        @Nullable DimensionOptions dimensionOptions = makeDimensionOptions(runtimeDimensionDescriptor);

        /* Mark the dimension options, to ignore it while world saving. */
        ((ExtendedDimensionOptions) (Object) dimensionOptions).fuji$setSaveDimensionOptions(false);

        /* Make the dimension instance. */
        Identifier dimensionIdentifier = RegistryHelper.makeIdentifierOrThrow(runtimeDimensionDescriptor.dimension);
        RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, dimensionIdentifier);
        ServerWorld dimension = new RuntimeDimension(server,
            Util.getMainWorkerExecutor(),
            server.session,
            worldProperties,
            worldRegistryKey,
            dimensionOptions,
            VoidWorldGenerationProgressListener.INSTANCE,
            runtimeDimensionDescriptor.isDebugWorld(),
            BiomeAccess.hashSeed(runtimeDimensionDescriptor.seed),
            ImmutableList.of(),
            runtimeDimensionDescriptor.shouldTickTime,
            makeRandomSequenceState(runtimeDimensionDescriptor));

        /* Do some post things for this dimension. */
        postRuntimeDimensionMake(dimension, runtimeDimensionDescriptor);

        /* Return the dimension instance with the dimension options. */
        return new Pair<>(dimension, dimensionOptions);
    }

    private static @NotNull RandomSequencesState makeRandomSequenceState(@NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        long seed = runtimeDimensionDescriptor.seed;
        return new RandomSequencesState(seed);
    }

    @SuppressWarnings("deprecation")
    private static void postRuntimeDimensionMake(ServerWorld dimension, @NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        // If the dimension type is THE_END, then start the dragon fight.
        if (DimensionTypes.THE_END_ID.toString().equals(runtimeDimensionDescriptor.dimension_type)) {
            dimension.setEnderDragonFight(new EnderDragonFight(dimension, dimension.getSeed(), EnderDragonFight.Data.DEFAULT));
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
        Registry<WorldPreset> worldPresetRegistry = RegistryHelper.getRegistry(RegistryKeys.WORLD_PRESET);
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
        RegistryEntry<DimensionType> dimensionTypeEntry = makeDimensionTypeEntry(runtimeDimensionDescriptor);
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
        Registry<DimensionOptions> dimensionRegistry = RegistryHelper.getRegistry(RegistryKeys.DIMENSION);
        Identifier dimensionTypeIdentifier = RegistryHelper.makeIdentifierOrThrow(dimensionDescriptor.dimension_type);
        @Nullable DimensionOptions existedDimensionOptions = dimensionRegistry.get(dimensionTypeIdentifier);
        if (existedDimensionOptions == null) {
            LogUtil.warn("Failed to make chunk generator for {}, there is no existed DimensionOptions for dimension type {}. Falling back to the `FlatChunkGenerator`.", dimensionDescriptor.getDimension(), dimensionTypeIdentifier);
            return makeFlatChunkGenerator(dimensionDescriptor);
        }

        // NOTE: Copy the existed chunk generator, to ensure the settings of chunk generator is identical.
        // NOTE: For vanilla Minecraft dimensions, they both use `NoiseChunkGenerator`. However, for mods that adds custom chunk generators, this method will return whatever the chunk generator the mod is using.
        return existedDimensionOptions.chunkGenerator();
    }

    private static @NotNull RegistryEntry<DimensionType> makeDimensionTypeEntry(RuntimeDimensionDescriptor dimensionDescriptor) {
        Identifier dimensionTypeIdentifier = RegistryHelper.makeIdentifierOrThrow(dimensionDescriptor.dimension_type);
        Optional<RegistryEntry<DimensionType>> dimensionTypeEntryOptional = RegistryHelper.getRegistryEntry(RegistryKeys.DIMENSION_TYPE, dimensionTypeIdentifier);
        if (dimensionTypeEntryOptional.isEmpty()) {
            throw new RuntimeException("Failed to make RegistryEntry<DimensionType> for dimension %s: The Optional<RegistryEntry<DimensionType>> null.".formatted(dimensionTypeIdentifier));
        }

        RegistryEntry<DimensionType> dimensionTypeRegistryEntry = dimensionTypeEntryOptional.get();
        if (dimensionTypeRegistryEntry.comp_349() == null) {
            throw new RuntimeException("Failed to make RegistryEntry<DimensionType> for dimension %s: The value of RegistryEntry<DimensionType>.comp_349() is null.".formatted(dimensionTypeIdentifier));
        }
        return dimensionTypeRegistryEntry;
    }

    private static @NotNull FlatChunkGenerator makeFlatChunkGenerator(RuntimeDimensionDescriptor dimensionDescriptor) {
        /* Make the default flat chunk generator config. */
        RegistryEntryLookup<Biome> biomeLookup = RegistryHelper.getRegistryEntryLookup(RegistryKeys.BIOME);
        RegistryEntryLookup<StructureSet> structureSetLookup = RegistryHelper.getRegistryEntryLookup(RegistryKeys.STRUCTURE_SET);
        RegistryEntryLookup<PlacedFeature> placedFeatureLookup = RegistryHelper.getRegistryEntryLookup(RegistryKeys.PLACED_FEATURE);
        FlatChunkGeneratorConfig flatChunkGeneratorConfig = FlatChunkGeneratorConfig.getDefaultConfig(biomeLookup, structureSetLookup, placedFeatureLookup);

        /* Make the parsed flat chunk generator config. */
        String presetString = dimensionDescriptor.chunkGeneratorParameters;
        if (presetString != null && !presetString.isBlank()) {
            RegistryEntryLookup<Block> blockLookup = RegistryHelper.getRegistryEntryLookup(RegistryKeys.BLOCK);
            flatChunkGeneratorConfig = FlatPresetParser.parsePresetString(blockLookup, biomeLookup, structureSetLookup, placedFeatureLookup, presetString, flatChunkGeneratorConfig);
        }

        return new FlatChunkGenerator(flatChunkGeneratorConfig);
    }
}
