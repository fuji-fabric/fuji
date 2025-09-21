package mod.fuji.module.initializer.world.manager.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
public class FlatPresetParser {

    private static final RegistryKey<Biome> BIOME_KEY = BiomeKeys.PLAINS;

    @Nullable
    private static FlatChunkGeneratorLayer parseLayerString(RegistryEntryLookup<Block> registryEntryLookup, String string, int i) {
        Optional<RegistryEntry.Reference<Block>> optional;
        int j;
        String string2;
        List<String> list = Splitter.on('*').limit(2).splitToList(string);
        if (list.size() == 2) {
            string2 = list.get(1);
            try {
                j = Math.max(Integer.parseInt(list.get(0)), 0);
            } catch (NumberFormatException numberFormatException) {
                LogUtil.error("Error while parsing flat world string", numberFormatException);
                return null;
            }
        } else {
            string2 = list.get(0);
            j = 1;
        }
        int k = Math.min(i + j, DimensionType.MAX_HEIGHT);
        int l = k - i;
        try {
            optional = registryEntryLookup.getOptional(RegistryKey.of(RegistryKeys.BLOCK, RegistryHelper.makeIdentifierOrThrow(string2)));
        } catch (Exception exception) {
            LogUtil.error("Error while parsing flat world string", exception);
            return null;
        }
        if (optional.isEmpty()) {
            LogUtil.error("Error while parsing flat world string => Unknown block, {}", (Object)string2);
            return null;
        }
        return new FlatChunkGeneratorLayer(l, optional.get().comp_349());
    }

    @SuppressWarnings({"MixedMutabilityReturnType", "StringSplitter"})
    private static List<FlatChunkGeneratorLayer> parsePresetLayersString(RegistryEntryLookup<Block> registryEntryLookup, String string) {
        ArrayList<FlatChunkGeneratorLayer> list = Lists.newArrayList();
        String[] strings = string.split(",");
        int i = 0;
        for (String string2 : strings) {
            FlatChunkGeneratorLayer flatChunkGeneratorLayer = parseLayerString(registryEntryLookup, string2, i);
            if (flatChunkGeneratorLayer == null) {
                return Collections.emptyList();
            }
            #if MC_VER < MC_1_21_6
            list.add(flatChunkGeneratorLayer);
            #elif MC_VER >= MC_1_21_6
            int j = DimensionType.MAX_HEIGHT - i;
            if (j <= 0) continue;
            list.add(flatChunkGeneratorLayer.withMaxThickness(j));
            #endif
            i += flatChunkGeneratorLayer.getThickness();
        }
        return list;
    }

    @SuppressWarnings("AssignmentExpression")
    public static FlatChunkGeneratorConfig parsePresetString(RegistryEntryLookup<Block> registryEntryLookup, RegistryEntryLookup<Biome> registryEntryLookup2, RegistryEntryLookup<StructureSet> registryEntryLookup3, RegistryEntryLookup<PlacedFeature> registryEntryLookup4, String string, FlatChunkGeneratorConfig flatChunkGeneratorConfig) {
        RegistryEntry.Reference<Biome> reference;
        Iterator<String> iterator = Splitter.on(';').split(string).iterator();
        if (!iterator.hasNext()) {
            return FlatChunkGeneratorConfig.getDefaultConfig(registryEntryLookup2, registryEntryLookup3, registryEntryLookup4);
        }
        List<FlatChunkGeneratorLayer> list = parsePresetLayersString(registryEntryLookup, iterator.next());
        if (list.isEmpty()) {
            return FlatChunkGeneratorConfig.getDefaultConfig(registryEntryLookup2, registryEntryLookup3, registryEntryLookup4);
        }
        RegistryEntry<Biome> registryEntry = reference = registryEntryLookup2.getOrThrow(BIOME_KEY);
        if (iterator.hasNext()) {
            String string2 = iterator.next();
            registryEntry = Optional.ofNullable(Identifier.tryParse(string2)).map(identifier -> RegistryKey.of(RegistryKeys.BIOME, identifier)).flatMap(registryEntryLookup2::getOptional).orElseGet(() -> {
                LogUtil.warn("Invalid biome: {}", (Object)string2);
                return reference;
            });
        }
        return flatChunkGeneratorConfig.with(list, flatChunkGeneratorConfig.getStructureOverrides(), registryEntry);
    }
}
