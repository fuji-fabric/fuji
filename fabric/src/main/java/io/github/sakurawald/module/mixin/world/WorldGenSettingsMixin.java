package io.github.sakurawald.module.mixin.world;

import net.minecraft.world.level.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import com.google.common.collect.Maps;
import io.github.sakurawald.module.initializer.world.accessor.IDimensionOptions;
import io.github.sakurawald.module.initializer.world.structure.FilteredRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
#endif


@Mixin(WorldGenSettings.class)
public class WorldGenSettingsMixin {

    #if MC_VER <= MC_1_20_4
    #elif MC_VER > MC_1_20_4
    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;)Lcom/mojang/serialization/DataResult;"
        , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenSettings;<init>(Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;)V"), index = 1)
    private static @NotNull DimensionOptionsRegistryHolder $wrapWorldGenSettings(DimensionOptionsRegistryHolder original) {
        Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions = original.comp_1014();
        var saveDimensions = Maps.filterEntries(dimensions, entry -> IDimensionOptions.SAVE_PROPERTIES_PREDICATE.test(entry.getValue()));
        return new DimensionOptionsRegistryHolder(saveDimensions);
    }

    // fix: failed to save world on `/save-all` for `fuji:1` dimension.
    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/registry/DynamicRegistryManager;)Lcom/mojang/serialization/DataResult;"
        , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;<init>(Lnet/minecraft/registry/Registry;)V"), index = 0)
    private static Registry<DimensionOptions> $wrapWorldGenSettings(Registry<DimensionOptions> registry) {
        return new FilteredRegistry<>(registry, IDimensionOptions.SAVE_PROPERTIES_PREDICATE);
    }
    #endif
}
