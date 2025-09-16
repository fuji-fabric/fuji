package io.github.sakurawald.fuji.core.command.argument.wrapper.impl;

import io.github.sakurawald.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.util.Identifier;

public class BiomeId extends SingularValue<Identifier> {
    public BiomeId(Identifier value) {
        super(value);
    }
}
