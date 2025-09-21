package mod.fuji.module.initializer.economy.command.argument.wrapper;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.util.Identifier;

public class CurrencyId extends SingularValue<Identifier> {
    public CurrencyId(Identifier value) {
        super(value);
    }
}
