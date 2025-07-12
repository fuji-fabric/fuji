package io.github.sakurawald.fuji.module.initializer.world.accessor;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import net.minecraft.world.dimension.DimensionOptions;

import java.util.function.Predicate;

public interface ExtendedDimensionOptions {

    Predicate<DimensionOptions> SAVE_PROPERTIES_PREDICATE = (it) -> {
        // the `it` will be null, for `fuji:1` dimension.
        boolean test = false;
        if (it != null) {
            test = ((ExtendedDimensionOptions) (Object) it).fuji$getSaveProperties();
        }

        LogUtil.debug("SAVE_PROPERTIES_PREDICATE: it = {}, test = {}", it, test);
        return test;
    };

    void fuji$setSaveProperties(boolean value);

    boolean fuji$getSaveProperties();
}
