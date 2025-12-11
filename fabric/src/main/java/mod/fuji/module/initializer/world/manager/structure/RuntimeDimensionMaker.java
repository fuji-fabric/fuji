package mod.fuji.module.initializer.world.manager.structure;

import com.google.common.collect.ImmutableList;
import java.util.concurrent.Executor;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.structure.BuiltinDimensionTypesIR;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.core.structure.Pair;
import mod.fuji.module.initializer.world.manager.accessor.ExtendedDimensionOptions;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.ChunkGeneratorType;
import mod.fuji.module.initializer.world.manager.service.FlatPresetParser;
import java.util.Optional;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RuntimeDimensionMaker {

    private static Executor getServerBackgroundExecutor() {
        #if MC_VER < MC_1_21_11
        return net.minecraft.Util.backgroundExecutor();
        #elif MC_VER >= MC_1_21_11
        return net.minecraft.util.Util.backgroundExecutor();
        #endif
    }

    public static Pair<ServerLevel, LevelStem> makeRuntimeDimension(@NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        MinecraftServer server = ServerHelper.getServer();

        /* Make the dimension properties. */
        // NOTE: Here we just mirror the `SaveProperties` from `minecraft:overworld`.
        RuntimeDimensionProperties worldProperties = makeWorldProperties(runtimeDimensionDescriptor);

        /* Make the dimension options. */
        @Nullable LevelStem dimensionOptions = makeDimensionOptions(runtimeDimensionDescriptor);

        /* Mark the dimension options, to ignore it while world saving. */
        ((ExtendedDimensionOptions) (Object) dimensionOptions).fuji$setSaveDimensionOptions(false);

        /* Make the dimension instance. */
        IdentifierIR dimensionIdentifier = IdentifierIR.makeIdentifierOrThrow(runtimeDimensionDescriptor.dimension);
        ResourceKey<Level> worldRegistryKey = ResourceKey.create(Registries.DIMENSION, dimensionIdentifier.getNativeValue());

        ServerLevel dimension = new RuntimeDimension(server,
            getServerBackgroundExecutor(),
            server.storageSource,
            worldProperties,
            worldRegistryKey,
            dimensionOptions,

            #if MC_VER < MC_1_21_9
            mod.fuji.module.initializer.world.manager.structure.util.VoidWorldGenerationProgressListener.INSTANCE,
            #elif MC_VER >= MC_1_21_9
            // The parameter is removed.
            #endif

            runtimeDimensionDescriptor.isDebugWorld(),
            BiomeManager.obfuscateSeed(runtimeDimensionDescriptor.seed),
            ImmutableList.of(),
            runtimeDimensionDescriptor.shouldTickTime,
            makeRandomSequenceState(runtimeDimensionDescriptor));

        /* Do some post things for this dimension. */
        postRuntimeDimensionMake(dimension, runtimeDimensionDescriptor);

        /* Return the dimension instance with the dimension options. */
        return new Pair<>(dimension, dimensionOptions);
    }

    private static @NotNull RuntimeDimensionProperties makeWorldProperties(@NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        MinecraftServer server = ServerHelper.getServer();
        return new RuntimeDimensionProperties(server.getWorldData(), runtimeDimensionDescriptor);
    }

    private static @NotNull RandomSequences makeRandomSequenceState(@NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        long seed = runtimeDimensionDescriptor.seed;

        #if MC_VER < MC_1_21_11
        return new RandomSequences(seed);
        #elif MC_VER >= MC_1_21_11
        return new RandomSequences();
        #endif
    }

    @SuppressWarnings("deprecation")
    private static void postRuntimeDimensionMake(ServerLevel dimension, @NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        // If the dimension type is THE_END, then start the dragon fight.
        if (BuiltinDimensionTypesIR.END.toString().equals(runtimeDimensionDescriptor.dimension_type)) {
            dimension.setDragonFight(new EndDragonFight(dimension, dimension.getSeed(), EndDragonFight.Data.DEFAULT));
        }
    }

    private static @NotNull LevelStem makeDimensionOptions(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        if (runtimeDimensionDescriptor.worldPresetType != null) {
            return makeDimensionOptionsWithWorldPreset(runtimeDimensionDescriptor);
        } else {
            return makeDimensionOptionsWithCustomization(runtimeDimensionDescriptor);
        }
    }

    private static @NotNull LevelStem makeDimensionOptionsWithWorldPreset(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        Registry<WorldPreset> worldPresetRegistry = RegistryHelper.getRegistry(Registries.WORLD_PRESET);
        assert runtimeDimensionDescriptor.worldPresetType != null;
        ResourceKey<WorldPreset> worldPresetKey = runtimeDimensionDescriptor.worldPresetType.toWorldPresetKey();
        WorldPreset worldPreset = RegistryHelper.getValue(worldPresetRegistry, worldPresetKey);
        if (worldPreset == null) {
            throw new RuntimeException("Failed to make DimensionOptions for dimension %s, the WorldPreset didn't existed in the registry.".formatted(runtimeDimensionDescriptor.dimension));
        }

        Optional<LevelStem> overworldDimensionOptionsOptional = worldPreset.overworld();
        if (overworldDimensionOptionsOptional.isEmpty()) {
            throw new RuntimeException("Failed to make DimensionOptions for dimension %s, the WorldPreset#getOverworld() returns null.".formatted(runtimeDimensionDescriptor.dimension));
        }

        return overworldDimensionOptionsOptional.get();
    }

    private static @NotNull LevelStem makeDimensionOptionsWithCustomization(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        /* Get an existing dimension options from registry. */
        Holder<DimensionType> dimensionTypeEntry = makeDimensionTypeEntry(runtimeDimensionDescriptor);
        ChunkGenerator chunkGenerator = makeChunkGenerator(runtimeDimensionDescriptor);

        // NOTE: Make a new DimensionOptions instance. (One DimensionOptions instance can only be used by a ServerWorld instance)
        return new LevelStem(dimensionTypeEntry, chunkGenerator);
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
        Registry<LevelStem> dimensionRegistry = RegistryHelper.getRegistry(Registries.LEVEL_STEM);
        IdentifierIR dimensionTypeIdentifier = IdentifierIR.makeIdentifierOrThrow(dimensionDescriptor.dimension_type);
        @Nullable LevelStem existedDimensionOptions = RegistryHelper.getValue(dimensionRegistry, dimensionTypeIdentifier);
        if (existedDimensionOptions == null) {
            LogUtil.warn("Failed to make chunk generator for {}, there is no existed DimensionOptions for dimension type {}. Falling back to the `FlatChunkGenerator`.", dimensionDescriptor.getDimension(), dimensionTypeIdentifier);
            return makeFlatChunkGenerator(dimensionDescriptor);
        }

        // NOTE: Copy the existed chunk generator, to ensure the settings of chunk generator is identical.
        // NOTE: For vanilla Minecraft dimensions, they both use `NoiseChunkGenerator`. However, for mods that adds custom chunk generators, this method will return whatever the chunk generator the mod is using.
        return existedDimensionOptions.generator();
    }

    private static @NotNull Holder<DimensionType> makeDimensionTypeEntry(RuntimeDimensionDescriptor dimensionDescriptor) {
        IdentifierIR dimensionTypeIdentifier = IdentifierIR.makeIdentifierOrThrow(dimensionDescriptor.dimension_type);
        Optional<Holder<DimensionType>> dimensionTypeEntryOptional = RegistryHelper.getRegistryEntry(Registries.DIMENSION_TYPE, dimensionTypeIdentifier);
        if (dimensionTypeEntryOptional.isEmpty()) {
            throw new RuntimeException("Failed to make RegistryEntry<DimensionType> for dimension %s: The Optional<RegistryEntry<DimensionType>> null.".formatted(dimensionTypeIdentifier));
        }

        Holder<DimensionType> dimensionTypeRegistryEntry = dimensionTypeEntryOptional.get();
        //noinspection ConstantValue
        if (dimensionTypeRegistryEntry.value() == null) {
            throw new RuntimeException("Failed to make RegistryEntry<DimensionType> for dimension %s: The value of RegistryEntry<DimensionType>.value() is null.".formatted(dimensionTypeIdentifier));
        }
        return dimensionTypeRegistryEntry;
    }

    private static @NotNull FlatLevelSource makeFlatChunkGenerator(RuntimeDimensionDescriptor dimensionDescriptor) {
        /* Make the default flat chunk generator config. */
        HolderGetter<Biome> biomeLookup = RegistryHelper.getRegistryEntryLookup(Registries.BIOME);
        HolderGetter<StructureSet> structureSetLookup = RegistryHelper.getRegistryEntryLookup(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> placedFeatureLookup = RegistryHelper.getRegistryEntryLookup(Registries.PLACED_FEATURE);
        FlatLevelGeneratorSettings flatChunkGeneratorConfig = FlatLevelGeneratorSettings.getDefault(biomeLookup, structureSetLookup, placedFeatureLookup);

        /* Make the parsed flat chunk generator config. */
        String presetString = dimensionDescriptor.chunkGeneratorParameters;
        if (presetString != null && !presetString.isBlank()) {
            HolderGetter<Block> blockLookup = RegistryHelper.getRegistryEntryLookup(Registries.BLOCK);
            flatChunkGeneratorConfig = FlatPresetParser.parsePresetString(blockLookup, biomeLookup, structureSetLookup, placedFeatureLookup, presetString, flatChunkGeneratorConfig);
        }

        return new FlatLevelSource(flatChunkGeneratorConfig);
    }
}
