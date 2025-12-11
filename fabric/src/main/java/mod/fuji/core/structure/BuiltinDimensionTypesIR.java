package mod.fuji.core.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public enum BuiltinDimensionTypesIR {

    OVERWORLD(
        #if MC_VER < MC_1_21_11
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD_EFFECTS
        #elif MC_VER >= MC_1_21_11
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD
        #endif
    ),
    NETHER(
        #if MC_VER < MC_1_21_11
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.NETHER_EFFECTS
        #elif MC_VER >= MC_1_21_11
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.NETHER
        #endif
    ),
    END(
        #if MC_VER < MC_1_21_11
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.END_EFFECTS
        #elif MC_VER >= MC_1_21_11
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.END
        #endif
    );

    final ResourceKey<@NotNull DimensionType> nativeValue;

    @Override
    public String toString() {
        return this.getNativeValue().toString();
    }
}
