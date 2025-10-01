package mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form;

import java.util.Optional;
import mod.fuji.module.initializer.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispDefvar extends LispSpecialForm {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments) {
        return LispFunctions.withCheckedVariableMutation(environment,arguments, lookupSymbol -> {
            /* Only eval the init-form and assign the value, when it's unbound. */
            Optional<LispObject> variableValue = lookupSymbol.getVariableValue();
            if (variableValue.isEmpty()) {
                LispObject second = arguments.get(1);
                second = second.eval(environment);
                environment.setVariableValue(lookupSymbol, second);
            }

            return lookupSymbol;
        });
    }

}
