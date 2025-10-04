package mod.fuji.evaluator.evaluator.structure;


import lombok.Value;
import mod.fuji.evaluator.evaluator.node.LispList;
import org.jetbrains.annotations.NotNull;

@Value
public class LambdaList {

    /**
     * The lambda list specified the names for the parameter of the function.
     * It only specifies the name of a parameter, and is not a type-specifier.
     */
    @NotNull LispList lambdaList;

    public static @NotNull LambdaList of(@NotNull LispList lambdaList) {
        return new LambdaList(lambdaList);
    }

    public static @NotNull LambdaList empty() {
        return of(LispList.of());
    }

    public void checkNumberOfArguments(@NotNull LispList arguments) {
//        int actual = arguments.size();
//        if (this.lambdaList.size() != actual) {
//            throw new LispInvalidNumberOfArgumentsException(actual);
//        }
    }

    public void bindParameterValues(@NotNull LispList values) {
        // check arity

        // process required args


        // process optional args

        // process key AND rest args.

    }

}
