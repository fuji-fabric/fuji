package mod.fuji.evaluator.evaluator.value.function.kinds.special_form;

import mod.fuji.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.value.LispList;
import mod.fuji.evaluator.evaluator.value.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispLambda extends LispSpecialForm {

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        LispFunctions.checkRequiredArity(arguments, 1);
//        LispList lambdaList = LispFunctions.checkType(arguments.get(0), LispList.class);

        LispList body = LispFunctions.cdr(arguments);
        LispObject apply = new LispProgn().apply(environment, body);
        return apply;
    }

}
