package mod.fuji.evaluator.evaluator.node.function.macro;

import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.LispSymbol;
import mod.fuji.evaluator.evaluator.node.function.LispFunction;
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
