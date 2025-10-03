package mod.fuji.module.initializer.evaluator.evaluator.node.function.macro;

import mod.fuji.module.initializer.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.LispFunction;
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
