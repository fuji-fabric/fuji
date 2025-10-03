package mod.fuji.evaluator.evaluator.node.function.standard.builtin;

import java.util.List;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.exception.LispInvalidNumberOfArgumentsException;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispNumber;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.function.standard.LispNativeFunction;
import org.jetbrains.annotations.NotNull;

public class SubtractFunction extends LispNativeFunction {

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        List<LispObject> objects = arguments.getObjects();
        if (objects.isEmpty()) {
            throw new LispInvalidNumberOfArgumentsException(0);
        }

        if (objects.size() == 1) {
            LispNumber lispNumber = (LispNumber) objects.get(0);
            return LispNumber.of(-lispNumber.getValue());
        }

        double result = -1;
        for (int i = 0; i < objects.size(); i++) {
            LispObject argument = objects.get(i);
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
