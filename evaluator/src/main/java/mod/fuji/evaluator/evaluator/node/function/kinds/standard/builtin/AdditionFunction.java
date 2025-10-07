package mod.fuji.evaluator.evaluator.node.function.kinds.standard.builtin;

import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispNumber;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.function.kinds.standard.LispNativeFunction;
import org.jetbrains.annotations.NotNull;

public class AdditionFunction extends LispNativeFunction {

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        double result = 0;
        for (LispObject argument : arguments) {
            if (argument instanceof LispNumber lispNumber) {
                result += lispNumber.getValue();
            } else {
                throw new LispEvaluationException("The value %s is not of type LispNumber.".formatted(argument));
            }
        }
        return LispNumber.of(result);
    }
}
