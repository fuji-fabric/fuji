package mod.fuji.module.initializer.evaluator.evaluator.node.function;

import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispInvalidNumberOfArgumentsException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import org.jetbrains.annotations.NotNull;

/**
 * The most important kind of `eval` is `apply`.
 * The most important kind of `apply` is `eval`.
 */
public abstract class LispFunction extends LispObject {

    /**
     * The lambda list specified the names for the parameter of the function.
     * It only specifies the name of a parameter, and is not a type-specifier.
     */
    final LispList lambdaList = LispList.of();


    /**
     * Apply the function to the given arguments.
     * Note that the <code>funcall</code> function should not evaluate the given arguments.
     * It will be called for Lisp special form, Lisp macro and Lisp standard function.
     * */
    public static @NotNull LispObject funcall(@NotNull LispSymbol functionNameSymbol, @NotNull Environment environment, @NotNull LispList arguments) {
        /* Get the function value. */
        LispFunction functionValue = environment
            .lookupSymbol(functionNameSymbol.getName())
            .getFunctionValue()
            .orElseThrow(() -> new LispEvaluationException("The function %s is undefined.".formatted(functionNameSymbol.getName())));

        /* Check the number of arguments. */
//        functionValue.checkNumberOfArguments(arguments);
//        Environment childEnvironment = new Environment();

        /* Call the function. */
        return functionValue.apply(environment, arguments);
    }

    @Override
    public abstract @NotNull LispObject eval(@NotNull Environment environment);

    public abstract @NotNull LispObject apply(@NotNull Environment environment, @NotNull LispList arguments);

    public void checkNumberOfArguments(@NotNull LispList arguments) {
        int actual = arguments.size();
        if (this.lambdaList.size() != actual) {
            throw new LispInvalidNumberOfArgumentsException(actual);
        }
    }
}
