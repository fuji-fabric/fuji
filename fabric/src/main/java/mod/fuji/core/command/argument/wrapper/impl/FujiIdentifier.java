package mod.fuji.core.command.argument.wrapper.impl;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.util.Identifier;

public class FujiIdentifier extends SingularValue<Identifier> {

    public FujiIdentifier(Identifier value) {
        super(value);
    }

}
