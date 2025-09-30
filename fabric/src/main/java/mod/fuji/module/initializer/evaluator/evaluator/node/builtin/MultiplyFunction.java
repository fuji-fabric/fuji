package mod.fuji.module.initializer.evaluator.evaluator.node.builtin;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispStandardFunction;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class MultiplyFunction extends LispStandardFunction {

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull List<LispObject> arguments) {
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
