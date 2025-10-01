package mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.module.initializer.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispDefconstant extends LispSpecialForm {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments) {
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
