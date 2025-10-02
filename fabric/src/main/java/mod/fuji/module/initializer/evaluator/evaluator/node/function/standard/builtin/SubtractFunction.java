package mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.builtin;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispInvalidNumberOfArgumentsException;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.LispStandardFunction;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class SubtractFunction extends LispStandardFunction {

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments) {
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
