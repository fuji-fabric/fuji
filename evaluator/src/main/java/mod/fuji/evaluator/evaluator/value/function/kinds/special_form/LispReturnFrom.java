package mod.fuji.evaluator.evaluator.value.function.kinds.special_form;

import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.value.LispList;
import mod.fuji.evaluator.evaluator.value.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispReturnFrom extends LispSpecialForm {

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        return null;
    }
}
