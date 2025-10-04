package mod.fuji.evaluator.evaluator.node.function;

import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.LispSymbol;
import org.jetbrains.annotations.NotNull;

/**
 * The most important kind of `eval` is `apply`.
 * The most important kind of `apply` is `eval`.
 */
public abstract class LispFunction extends LispObject {

    @Override
    public abstract @NotNull LispObject eval(@NotNull LispEnvironment environment);

    public abstract @NotNull LispObject apply(@NotNull LispEnvironment environment, @NotNull LispList arguments);

    /**
     * Apply the function to the given arguments.
     * Note that the <code>funcall</code> function should not evaluate the given arguments.
     * It will be called for Lisp special form, Lisp macro and Lisp standard function.
     * */
    public static @NotNull LispObject funcall(@NotNull LispSymbol functionNameSymbol, @NotNull LispEnvironment environment, @NotNull LispList arguments) {
        /* Get the function value. */
        LispFunction functionValue = environment
            .lookupSymbolByName(functionNameSymbol.getName())
            .getFunctionValue()
            .orElseThrow(() -> new LispEvaluationException("The function %s is undefined.".formatted(functionNameSymbol.getName())));

        /* Check the number of arguments. */
//        functionValue.checkNumberOfArguments(arguments);
//        Environment childEnvironment = new Environment();

        /* Call the function. */
        return functionValue.apply(environment, arguments);
    }

}
