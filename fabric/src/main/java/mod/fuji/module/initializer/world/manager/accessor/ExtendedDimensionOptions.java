package mod.fuji.module.initializer.world.manager.accessor;

import net.minecraft.world.dimension.DimensionOptions;

import java.util.function.Predicate;

public interface ExtendedDimensionOptions {

    Predicate<DimensionOptions> SAVE_DIMENSION_OPTIONS_PREDICATE = (it) -> {
        // NOTE: The `DimensionOptions` for `runtime dimensions` will be `null`, because we only mirror existed DimensionOptions, we never register our one.
        if (it == null) return false;

        return ((ExtendedDimensionOptions) (Object) it).fuji$getSaveDimensionOptions();
    };

    void fuji$setSaveDimensionOptions(boolean value);

    boolean fuji$getSaveDimensionOptions();
}
