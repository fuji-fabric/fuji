package mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form;

import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class PrognSpecialForm extends LispSpecialForm {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments) {
        @NotNull LispObject value = Environment.NIL;

        for (LispObject argument : arguments.getObjects()) {
            value = argument.eval(environment);
        }

        return value;
    }
}
