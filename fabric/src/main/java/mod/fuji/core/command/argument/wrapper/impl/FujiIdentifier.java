package mod.fuji.core.command.argument.wrapper.impl;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.resources.ResourceLocation;

public class FujiIdentifier extends SingularValue<ResourceLocation> {

    public FujiIdentifier(ResourceLocation value) {
        super(value);
    }

}
