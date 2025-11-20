package mod.fuji.module.initializer.economy.command.argument.wrapper;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.resources.ResourceLocation;

public class CurrencyId extends SingularValue<ResourceLocation> {
    public CurrencyId(ResourceLocation value) {
        super(value);
    }
}
