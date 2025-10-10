package mod.fuji.evaluator.evaluator.value.function.kinds.standard;

import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.value.LispList;
import mod.fuji.evaluator.evaluator.value.LispObject;
import mod.fuji.evaluator.evaluator.value.function.LispFunction;
import org.jetbrains.annotations.NotNull;

public abstract class LispPrimitiveFunction extends LispFunction {

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        return this;
    }

    @Override
    public abstract @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments);

}
