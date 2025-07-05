package io.github.sakurawald.fuji.module.initializer.economy.command.argument.wrapper;

import io.github.sakurawald.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.util.Identifier;

public class CurrencyId extends SingularValue<Identifier> {
    public CurrencyId(Identifier value) {
        super(value);
    }
}
