package mod.fuji.evaluator.evaluator.node.function.lambda;


import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.function.lambda.parameter.ParameterSpecifier;
import org.jetbrains.annotations.NotNull;

@Data
public class LambdaList {

    /**
     * The lambda list specifies the names for the parameter of the function.
     * It only specifies the name of a parameter, and is not a type-specifier.
     */
    @NotNull LispList lambdaList;
    @NotNull List<ParameterSpecifier> parameterSpecifiers;

    public static @NotNull LambdaList of(@NotNull LispList lambdaList) {
        LambdaList newValue = new LambdaList(lambdaList, new ArrayList<>());
        newValue.parameterSpecifiers = LambdaListParser.parse(newValue.lambdaList);
        return newValue;
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

    public void bindParameterValues(@NotNull LispList argumentValues) {
        // check arity

        // process required args


        // process optional args

        // process key AND rest args.

    }

}
