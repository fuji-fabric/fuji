package mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form;

import mod.fuji.module.initializer.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.module.initializer.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispDefparameter extends LispSpecialForm {

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        return this;
    }

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
