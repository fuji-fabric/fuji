package mod.fuji.module.initializer.evaluator.evaluator.node.builtin;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.InvalidNumberOfArgumentsException;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispFunction;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class DivideFunction extends LispFunction {

    @Override
    public @NotNull LispObject call(@NotNull Environment environment, @NotNull List<LispObject> arguments) {
        if (arguments.isEmpty()) {
            throw new InvalidNumberOfArgumentsException(0);
        }

        double result = 1;
        for (LispObject argument : arguments) {
            if (argument instanceof LispNumber lispNumber) {
                result /= lispNumber.getValue();
            } else {
                throw new LispEvaluationException("The value %s is not of type LispNumber.".formatted(argument));
            }
        }

        return LispNumber.of(result);
    }
}
