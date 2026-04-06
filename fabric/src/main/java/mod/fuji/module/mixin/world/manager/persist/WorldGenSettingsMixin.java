package mod.fuji.module.mixin.world.manager.persist;

import mod.fuji.core.document.annotation.TestCase;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER <= MC_1_20_4
import org.spongepowered.asm.mixin.injection.ModifyVariable;
#elif MC_VER > MC_1_20_4
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
#endif

#if MC_VER < MC_26_1
import mod.fuji.module.initializer.world.manager.accessor.ExtendedDimensionOptions;
import mod.fuji.module.initializer.world.manager.structure.util.FilteredRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
#elif MC_VER >= MC_26_1
#endif

@TestCase(action = "Issue the `/save-all` command without the installation of `fabric-api` mod.", targets = "The runtime dimensions should be saved.")
@Mixin(WorldGenSettings.class)
public class WorldGenSettingsMixin {

    #if MC_VER <= MC_1_20_4
    @ModifyVariable(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/world/level/levelgen/WorldDimensions;)Lcom/mojang/serialization/DataResult;", at = @At("HEAD"), argsOnly = true)
    private static WorldDimensions $wrapWorldGenSettings(WorldDimensions dimensionOptionsRegistryHolder) {
        Registry<LevelStem> dimensions = dimensionOptionsRegistryHolder.dimensions();
        FilteredRegistry<LevelStem> filteredDimensions = new FilteredRegistry<>(dimensions, ExtendedDimensionOptions.SAVE_DIMENSION_OPTIONS_PREDICATE);
        return new WorldDimensions(filteredDimensions);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/core/RegistryAccess;)Lcom/mojang/serialization/DataResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldDimensions;<init>(Lnet/minecraft/core/Registry;)V"), index = 0)
    private static Registry<LevelStem> $wrapWorldGenSettings(Registry<LevelStem> comp_1014) {
        Registry<LevelStem> dimensions = comp_1014;
        FilteredRegistry<LevelStem> filteredDimensions = new FilteredRegistry<>(dimensions, ExtendedDimensionOptions.SAVE_DIMENSION_OPTIONS_PREDICATE);
        return filteredDimensions;
    }

    #elif MC_VER > MC_1_20_4 && MC_VER < MC_26_1
    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/world/level/levelgen/WorldDimensions;)Lcom/mojang/serialization/DataResult;"
        , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldGenSettings;<init>(Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/world/level/levelgen/WorldDimensions;)V"), index = 1)
    private static @NotNull WorldDimensions $wrapWorldGenSettings(WorldDimensions original) {
        Map<ResourceKey<LevelStem>, LevelStem> dimensions = original.dimensions();
        var saveDimensions = Maps.filterEntries(dimensions, entry -> ExtendedDimensionOptions.SAVE_DIMENSION_OPTIONS_PREDICATE.test(entry.getValue()));
        return new WorldDimensions(saveDimensions);
    }

    @ModifyArg(method = "encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/core/RegistryAccess;)Lcom/mojang/serialization/DataResult;"
        , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldDimensions;<init>(Lnet/minecraft/core/Registry;)V"), index = 0)
    private static Registry<LevelStem> $wrapWorldGenSettings(Registry<LevelStem> registry) {
        return new FilteredRegistry<>(registry, ExtendedDimensionOptions.SAVE_DIMENSION_OPTIONS_PREDICATE);
    }
    #elif MC_VER >= MC_26_1
    // This mixin is not needed.
    #endif
}
