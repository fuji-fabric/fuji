package mod.fuji.module.initializer.evaluator.evaluator.node.function.standard;


import java.util.ArrayList;
import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.LispFunction;
import org.jetbrains.annotations.NotNull;

public abstract class LispStandardFunction extends LispFunction {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public abstract @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments);

    public static @NotNull LispObject funcall(@NotNull LispSymbol functionNameSymbol, @NotNull Environment environment, @NotNull LispList arguments) {
        /* Eval the function arguments. */
        LispList args = LispList.of();
        for (LispObject argument : arguments.getObjects()) {
            LispObject arg = argument.eval(environment);
            args.getObjects().add(arg);
        }
//        Environment childEnvironment = new Environment();

        /* Get the function value. */
        LispFunction functionValue = environment
            .lookupSymbol(functionNameSymbol.getName())
            .getFunctionValue()
            .orElseThrow(() -> new LispEvaluationException("The function %s is undefined.".formatted(functionNameSymbol.getName())));


        /* Call the function. */
        return functionValue.apply(environment, args);
    }


}
