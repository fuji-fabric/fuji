package mod.fuji.evaluator.evaluator.node.function.standard;

import lombok.AllArgsConstructor;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.function.LispFunction;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public abstract class LispDefinedFunction extends LispFunction {

    final LispList bodyForms;

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        throw new UnsupportedOperationException();
    }

}
