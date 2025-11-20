package mod.fuji.core.command.argument.wrapper.impl;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.resources.ResourceLocation;

public class BiomeId extends SingularValue<ResourceLocation> {
    public BiomeId(ResourceLocation value) {
        super(value);
    }
}
