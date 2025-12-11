package mod.fuji.core.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

@Getter
@AllArgsConstructor
public enum BuiltinDimensionTypesIR {

    OVERWORLD(
        IdentifierIR.of(BuiltinDimensionTypes.OVERWORLD_EFFECTS)
    ),
    NETHER(
        IdentifierIR.of(BuiltinDimensionTypes.NETHER_EFFECTS)
    ),
    END(
        IdentifierIR.of(BuiltinDimensionTypes.END_EFFECTS)
    );

    @SuppressWarnings("ImmutableEnumChecker")
    final IdentifierIR nativeValue;

    @Override
    public String toString() {
        return this.getNativeValue().toString();
    }
}
