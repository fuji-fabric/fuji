package mod.fuji.evaluator.evaluator.node.function.special_form;

import mod.fuji.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispLambda extends LispSpecialForm {

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments) {
        LispFunctions.checkRequiredArity(arguments, 1);
//        LispList lambdaList = LispFunctions.checkType(arguments.get(0), LispList.class);

        LispList body = LispFunctions.cdr(arguments);
        LispObject apply = new LispProgn().apply(environment, body);
        return apply;
    }

}
