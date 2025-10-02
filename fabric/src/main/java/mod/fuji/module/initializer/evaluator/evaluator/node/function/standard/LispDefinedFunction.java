package mod.fuji.module.initializer.evaluator.evaluator.node.function.standard;


import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.LispFunction;
import org.jetbrains.annotations.NotNull;

public abstract class LispDefinedFunction extends LispFunction {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments) {
        throw new UnsupportedOperationException();
    }

}
