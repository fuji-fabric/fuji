package mod.fuji.module.initializer.evaluator.evaluator.node.builtin;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.InvalidNumberOfArgumentsException;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispFunction;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class SubtractFunction extends LispFunction {

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull List<LispObject> arguments) {
        if (arguments.isEmpty()) {
            throw new InvalidNumberOfArgumentsException(0);
        }

        if (arguments.size() == 1) {
            LispNumber lispNumber = (LispNumber) arguments.get(0);
            return LispNumber.of(-lispNumber.getValue());
        }

        double result = -1;
        for (int i = 0; i < arguments.size(); i++) {
            LispObject argument = arguments.get(i);
            if (argument instanceof LispNumber lispNumber) {
                if (i == 0) {
                    result = lispNumber.getValue();
                    continue;
                }

                result -= lispNumber.getValue();
            } else {
                throw new LispEvaluationException("The value %s is not of type LispNumber.".formatted(argument));
            }
        }

        return LispNumber.of(result);
    }
}
