package mod.fuji.evaluator.evaluator.node.function.special_form;

import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispProgn extends LispSpecialForm {

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        @NotNull LispObject value = LispEnvironment.NIL;

        for (LispObject argument : arguments) {
            value = argument.eval(environment);
        }

        return value;
    }
}
