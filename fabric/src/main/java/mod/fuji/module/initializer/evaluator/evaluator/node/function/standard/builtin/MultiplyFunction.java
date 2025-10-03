package mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.builtin;

import mod.fuji.module.initializer.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.LispNativeFunction;
import org.jetbrains.annotations.NotNull;

public class MultiplyFunction extends LispNativeFunction {

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        double result = 1;
        for (LispObject argument : arguments) {
            if (argument instanceof LispNumber lispNumber) {
                result *= lispNumber.getValue();
            } else {
                throw new LispEvaluationException("The value %s is not of type LispNumber.".formatted(argument));
            }
        }
        return LispNumber.of(result);
    }
}
