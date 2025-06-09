package io.github.sakurawald.module.mixin.world;

import io.github.sakurawald.module.initializer.world.accessor.IDimensionOptions;
import net.minecraft.world.dimension.DimensionOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DimensionOptions.class)
public class DimensionOptionsMixin implements IDimensionOptions {

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
