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
import net.minecraft.world.level.block.Block;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
public class FlatPresetParser {

    private static final ResourceKey<Biome> BIOME_KEY = Biomes.PLAINS;

    @Nullable
    private static FlatLayerInfo parseLayerString(HolderGetter<Block> registryEntryLookup, String string, int i) {
        Optional<Holder.Reference<Block>> optional;
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
        int k = Math.min(i + j, DimensionType.Y_SIZE);
        int l = k - i;
        try {
            optional = registryEntryLookup.get(ResourceKey.create(Registries.BLOCK, RegistryHelper.makeIdentifierOrThrow(string2)));
        } catch (Exception exception) {
            LogUtil.error("Error while parsing flat world string", exception);
            return null;
        }
        if (optional.isEmpty()) {
            LogUtil.error("Error while parsing flat world string => Unknown block, {}", (Object)string2);
            return null;
        }
        return new FlatLayerInfo(l, optional.get().value());
    }

    @SuppressWarnings({"MixedMutabilityReturnType", "StringSplitter"})
    private static List<FlatLayerInfo> parsePresetLayersString(HolderGetter<Block> registryEntryLookup, String string) {
        ArrayList<FlatLayerInfo> list = Lists.newArrayList();
        String[] strings = string.split(",");
        int i = 0;
        for (String string2 : strings) {
            FlatLayerInfo flatChunkGeneratorLayer = parseLayerString(registryEntryLookup, string2, i);
            if (flatChunkGeneratorLayer == null) {
                return Collections.emptyList();
            }
            #if MC_VER < MC_1_21_6
            list.add(flatChunkGeneratorLayer);
            #elif MC_VER >= MC_1_21_6
            int j = DimensionType.Y_SIZE - i;
            if (j <= 0) continue;
            list.add(flatChunkGeneratorLayer.heightLimited(j));
            #endif
            i += flatChunkGeneratorLayer.getHeight();
        }
        return list;
    }

    @SuppressWarnings("AssignmentExpression")
    public static FlatLevelGeneratorSettings parsePresetString(HolderGetter<Block> registryEntryLookup, HolderGetter<Biome> registryEntryLookup2, HolderGetter<StructureSet> registryEntryLookup3, HolderGetter<PlacedFeature> registryEntryLookup4, String string, FlatLevelGeneratorSettings flatChunkGeneratorConfig) {
        Holder.Reference<Biome> reference;
        Iterator<String> iterator = Splitter.on(';').split(string).iterator();
        if (!iterator.hasNext()) {
            return FlatLevelGeneratorSettings.getDefault(registryEntryLookup2, registryEntryLookup3, registryEntryLookup4);
        }
        List<FlatLayerInfo> list = parsePresetLayersString(registryEntryLookup, iterator.next());
        if (list.isEmpty()) {
            return FlatLevelGeneratorSettings.getDefault(registryEntryLookup2, registryEntryLookup3, registryEntryLookup4);
        }
        Holder<Biome> registryEntry = reference = registryEntryLookup2.getOrThrow(BIOME_KEY);
        if (iterator.hasNext()) {
            String string2 = iterator.next();
            registryEntry = Optional.ofNullable(ResourceLocation.tryParse(string2)).map(identifier -> ResourceKey.create(Registries.BIOME, identifier)).flatMap(registryEntryLookup2::get).orElseGet(() -> {
                LogUtil.warn("Invalid biome: {}", (Object)string2);
                return reference;
            });
        }
        return flatChunkGeneratorConfig.withBiomeAndLayers(list, flatChunkGeneratorConfig.structureOverrides(), registryEntry);
    }
}
