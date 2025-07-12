package io.github.sakurawald.fuji.module.mixin.world.persist;

import io.github.sakurawald.fuji.module.initializer.world.accessor.ExtendedDimensionOptions;
import net.minecraft.world.dimension.DimensionOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DimensionOptions.class)
public class DimensionOptionsMixin implements ExtendedDimensionOptions {

    @Unique
    private boolean fuji$saveProperties = true;

    @Override
    public void fuji$setSaveProperties(boolean value) {
        this.fuji$saveProperties = value;
    }

    @Override
    public boolean fuji$getSaveProperties() {
        return this.fuji$saveProperties;
    }
}
