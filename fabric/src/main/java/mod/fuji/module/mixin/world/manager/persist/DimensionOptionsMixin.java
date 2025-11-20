package mod.fuji.module.mixin.world.manager.persist;

import mod.fuji.module.initializer.world.manager.accessor.ExtendedDimensionOptions;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelStem.class)
public class DimensionOptionsMixin implements ExtendedDimensionOptions {

    @Unique
    private boolean fuji$saveProperties = true;

    @Override
    public void fuji$setSaveDimensionOptions(boolean value) {
        this.fuji$saveProperties = value;
    }

    @Override
    public boolean fuji$getSaveDimensionOptions() {
        return this.fuji$saveProperties;
    }
}
