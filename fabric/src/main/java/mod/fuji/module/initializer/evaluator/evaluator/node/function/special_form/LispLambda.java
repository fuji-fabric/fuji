package mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form;

import com.google.errorprone.annotations.Keep;
import mod.fuji.core.annotation.Unused;
import mod.fuji.module.initializer.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispLambda extends LispSpecialForm {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments) {
        LispFunctions.checkRequiredArity(arguments, 1);
        @Unused
        LispList lambdaList = LispFunctions.checkType(arguments.get(0), LispList.class);

        LispList body = LispFunctions.cdr(arguments);
        LispObject apply = new LispProgn().apply(environment, body);
        return apply;
    }

}
