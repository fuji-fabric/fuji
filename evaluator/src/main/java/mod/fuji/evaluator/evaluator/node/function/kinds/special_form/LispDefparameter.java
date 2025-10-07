package mod.fuji.evaluator.evaluator.node.function.kinds.special_form;

import mod.fuji.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispDefparameter extends LispSpecialForm {

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        return LispFunctions.withCheckedVariableMutation(environment, arguments, lookupSymbol -> {
            if (lookupSymbol.isConstantVariableValue()) {
                throw new LispEvaluationException("Cannot proclaim a CONSTANT variable: %s".formatted(lookupSymbol.getName()));
            }

            LispObject second = arguments.get(1);
            second = second.eval(environment);
            environment.setVariableValue(lookupSymbol, second);

            return lookupSymbol;
        });
    }

}
