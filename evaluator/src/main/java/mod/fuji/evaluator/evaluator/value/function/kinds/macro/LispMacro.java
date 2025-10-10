package mod.fuji.evaluator.evaluator.value.function.kinds.macro;

import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.value.LispList;
import mod.fuji.evaluator.evaluator.value.LispObject;
import mod.fuji.evaluator.evaluator.value.LispSymbol;
import mod.fuji.evaluator.evaluator.value.function.LispFunction;
import org.jetbrains.annotations.NotNull;

public class LispMacro extends LispFunction {

    public static boolean isMacro(@NotNull LispSymbol lispSymbol) {
        return false;
    }

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        return this;
    }
}
