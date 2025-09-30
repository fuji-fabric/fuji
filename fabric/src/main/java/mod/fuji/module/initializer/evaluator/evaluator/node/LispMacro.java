package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import org.jetbrains.annotations.NotNull;

public class LispMacro extends LispFunction {

    public static boolean isMacro(@NotNull LispSymbol lispSymbol) {
        return false;
    }

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull List<LispObject> arguments) {
        return this;
    }
}
