package mod.fuji.module.initializer.evaluator.evaluator.node.function;

import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispInvalidNumberOfArgumentsException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
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
