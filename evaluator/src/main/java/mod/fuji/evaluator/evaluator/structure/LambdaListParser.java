package mod.fuji.evaluator.evaluator.structure;

import com.google.errorprone.annotations.Keep;
import mod.fuji.evaluator.evaluator.node.LispList;
import org.jetbrains.annotations.NotNull;

public class LambdaListParser {

    @Keep
    public static int parseLambdaList(@NotNull LispList lambdaList) {
        // TEST: The order of argument types specified in a lambda list.
        return 0;
    }

    @Keep
    private static boolean isParameterSpecifier() {
        return false;
    }

    @Keep
    private static boolean isLambdaListKeyword() {
        return false;
    }

    @Keep
    private static int parseOptionalArguments() {
        return 0;
    }

    @Keep
    private static int parseRestArguments() {
        return 0;
    }

    @Keep
    private static int parseKeyArguments() {
        return 0;
    }

    @Keep
    private static int parseAuxArguments() {
        return 0;
    }

}
