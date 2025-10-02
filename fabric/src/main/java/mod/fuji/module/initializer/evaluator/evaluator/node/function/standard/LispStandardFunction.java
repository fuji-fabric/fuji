package mod.fuji.module.initializer.evaluator.evaluator.node.function.standard;


import mod.fuji.module.initializer.evaluator.evaluator.auxliary.LispFunctions;
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
        LispList args = LispFunctions.evalForms(environment, arguments);

        /* Get the function value. */
        LispFunction functionValue = environment
            .lookupSymbol(functionNameSymbol.getName())
            .getFunctionValue()
            .orElseThrow(() -> new LispEvaluationException("The function %s is undefined.".formatted(functionNameSymbol.getName())));

        /* Check the number of arguments. */
//        functionValue.checkNumberOfArguments(arguments);

//        Environment childEnvironment = new Environment();

        /* Call the function. */
        return functionValue.apply(environment, args);
    }

}
