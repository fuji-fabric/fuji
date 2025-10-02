package mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.builtin;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispInvalidNumberOfArgumentsException;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.LispNativeFunction;
import org.jetbrains.annotations.NotNull;

public class DivideFunction extends LispNativeFunction {

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments) {
        List<LispObject> objects = arguments.getObjects();
        if (objects.isEmpty()) {
            throw new LispInvalidNumberOfArgumentsException(0);
        }

        double result = 1;
        for (LispObject argument : objects) {
            if (argument instanceof LispNumber lispNumber) {
                result /= lispNumber.getValue();
            } else {
                throw new LispEvaluationException("The value %s is not of type LispNumber.".formatted(argument));
            }
        }

        return LispNumber.of(result);
    }
}
