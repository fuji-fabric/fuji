package mod.fuji.module.mixin.world.manager.persist;

import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.world.manager.accessor.ExtendedDimensionOptions;
import mod.fuji.module.initializer.world.manager.structure.util.FilteredRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.level.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

#if MC_VER <= MC_1_20_4
import org.spongepowered.asm.mixin.injection.ModifyVariable;
#elif MC_VER > MC_1_20_4
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.registry.RegistryKey;
#endif

@TestCase(action = "Issue the `/save-all` command without the installation of `fabric-api` mod.", targets = "The runtime dimensions should be saved.")
@Mixin(WorldGenSettings.class)
public class WorldGenSettingsMixin {

    #if MC_VER <= MC_1_20_4
    @ModifyVariable(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;)Lcom/mojang/serialization/DataResult;", at = @At("HEAD"), argsOnly = true)
    private static DimensionOptionsRegistryHolder $wrapWorldGenSettings(DimensionOptionsRegistryHolder dimensionOptionsRegistryHolder) {
        Registry<DimensionOptions> dimensions = dimensionOptionsRegistryHolder.comp_1014();
        FilteredRegistry<DimensionOptions> filteredDimensions = new FilteredRegistry<>(dimensions, ExtendedDimensionOptions.SAVE_DIMENSION_OPTIONS_PREDICATE);
        return new DimensionOptionsRegistryHolder(filteredDimensions);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/registry/DynamicRegistryManager;)Lcom/mojang/serialization/DataResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;<init>(Lnet/minecraft/registry/Registry;)V"), index = 0)
    private static Registry<DimensionOptions> $wrapWorldGenSettings(Registry<DimensionOptions> comp_1014) {
        Registry<DimensionOptions> dimensions = comp_1014;
        FilteredRegistry<DimensionOptions> filteredDimensions = new FilteredRegistry<>(dimensions, ExtendedDimensionOptions.SAVE_DIMENSION_OPTIONS_PREDICATE);
        return filteredDimensions;
    }

    #elif MC_VER > MC_1_20_4
    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;)Lcom/mojang/serialization/DataResult;"
        , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenSettings;<init>(Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;)V"), index = 1)
    private static @NotNull DimensionOptionsRegistryHolder $wrapWorldGenSettings(DimensionOptionsRegistryHolder original) {
        Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions = original.comp_1014();
        var saveDimensions = Maps.filterEntries(dimensions, entry -> ExtendedDimensionOptions.SAVE_DIMENSION_OPTIONS_PREDICATE.test(entry.getValue()));
        return new DimensionOptionsRegistryHolder(saveDimensions);
    }

    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/gen/GeneratorOptions;Lnet/minecraft/registry/DynamicRegistryManager;)Lcom/mojang/serialization/DataResult;"
        , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionOptionsRegistryHolder;<init>(Lnet/minecraft/registry/Registry;)V"), index = 0)
    private static Registry<DimensionOptions> $wrapWorldGenSettings(Registry<DimensionOptions> registry) {
        return new FilteredRegistry<>(registry, ExtendedDimensionOptions.SAVE_DIMENSION_OPTIONS_PREDICATE);
    }
    #endif
}
