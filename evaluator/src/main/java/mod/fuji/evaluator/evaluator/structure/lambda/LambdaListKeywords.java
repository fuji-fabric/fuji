package mod.fuji.evaluator.evaluator.structure.lambda;

import java.util.List;
import mod.fuji.evaluator.evaluator.node.LispSymbol;
import org.jetbrains.annotations.NotNull;

/**
 * Define the <code>lambda list keywords</code>.
 */
public class LambdaListKeywords {
    /**
     * This is a dummy symbol, to represent the <code>required argument type</code>.
     */
    public static final LispSymbol REQUIRED_ARGUMENT_KEYWORD = LispSymbol.of("&required");
    public static final LispSymbol OPTIONAL_ARGUMENT_KEYWORD = LispSymbol.of("&optional");
    public static final LispSymbol REST_ARGUMENT_KEYWORD = LispSymbol.of("&rest");
    public static final LispSymbol KEY_ARGUMENT_KEYWORD = LispSymbol.of("&key");

    public static final LispSymbol AUX_ARGUMENT_KEYWORD = LispSymbol.of("&aux");

    public static final LispSymbol BODY_ARGUMENT_KEYWORD = LispSymbol.of("&body");
    public static final LispSymbol WHOLE_ARGUMENT_KEYWORD = LispSymbol.of("&whole");

    /**
     * For a state whose index is N, the acceptable next states are the state whose index > N.
     */
    private static final List<LispSymbol> LAMBDA_LIST_KEYWORDS_ORDER = List.of(
        REQUIRED_ARGUMENT_KEYWORD,
        OPTIONAL_ARGUMENT_KEYWORD,
        REST_ARGUMENT_KEYWORD,
        KEY_ARGUMENT_KEYWORD
    );

    public static int indexOfLambdaListKeyword(@NotNull LispSymbol lispSymbol) {
        for (int i = 0; i < LAMBDA_LIST_KEYWORDS_ORDER.size(); i++) {
            LispSymbol current = LAMBDA_LIST_KEYWORDS_ORDER.get(i);
            if (current.getName().equals(lispSymbol.getName())) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isLambdaListKeyword(@NotNull LispSymbol lispSymbol) {
        return indexOfLambdaListKeyword(lispSymbol) != -1;
    }

    public static @NotNull List<LispSymbol> getNextLambdaListKeywords(@NotNull LispSymbol lispSymbol) {
        int index = indexOfLambdaListKeyword(lispSymbol);
        if (index == -1) {
            return List.of();
        }

        return LAMBDA_LIST_KEYWORDS_ORDER.subList(index, LAMBDA_LIST_KEYWORDS_ORDER.size());
    }
}
