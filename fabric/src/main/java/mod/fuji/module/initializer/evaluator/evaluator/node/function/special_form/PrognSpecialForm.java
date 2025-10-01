package mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class PrognSpecialForm extends LispSpecialForm {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull List<LispObject> arguments) {
        @NotNull LispObject value = Environment.NIL;

        for (LispObject argument : arguments) {
            value = argument.eval(environment);
        }

        return value;
    }
}
