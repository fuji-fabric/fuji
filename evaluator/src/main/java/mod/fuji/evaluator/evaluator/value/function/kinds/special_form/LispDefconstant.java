package mod.fuji.evaluator.evaluator.value.function.kinds.special_form;

import mod.fuji.evaluator.auxiliary.LogUtil;
import mod.fuji.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.value.LispList;
import mod.fuji.evaluator.evaluator.value.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispDefconstant extends LispSpecialForm {

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        return LispFunctions.withCheckedVariableMutation(environment, arguments, lookupSymbol -> {
            LispObject second = arguments.get(1);
            second = second.eval(environment);

            if (lookupSymbol.getVariableValue().isEmpty()) {
                environment.setNamedConstant(lookupSymbol, second);
            } else {
                if (lookupSymbol.isConstantVariableValue()) {
                    LispObject oldValue = lookupSymbol.getVariableValue().get();
                    throw new LispEvaluationException("The constant %s is being redefined. (from %s to %s)".formatted(lookupSymbol.getName(), oldValue, second));
                } else {
                    LogUtil.warn("WARNING: redefining special {} to be a constant.", lookupSymbol.getName());
                    environment.setNamedConstant(lookupSymbol, second);
                }
            }

            return lookupSymbol;
        });
    }
}
