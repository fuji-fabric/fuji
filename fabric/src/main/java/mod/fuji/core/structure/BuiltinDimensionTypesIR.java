package mod.fuji.core.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public enum BuiltinDimensionTypesIR {

    OVERWORLD(
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.OVERWORLD
    ),
    NETHER(
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.NETHER
    ),
    END(
        net.minecraft.world.level.dimension.BuiltinDimensionTypes.END
    );

    @SuppressWarnings("ImmutableEnumChecker")
    final ResourceKey<@NotNull DimensionType> nativeValue;

    @Override
    public String toString() {
        IdentifierIR identifier = RegistryHelper.getIdentifier(this.getNativeValue());
        return identifier.toString();
    }

}
