package mod.fuji.module.initializer.evaluator.evaluator.auxliary;

import java.util.Collections;
import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispFunctions {

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static @NotNull LispObject car(@NotNull List<LispObject> list) {
        if (list.isEmpty()) return Environment.NIL;
        return list.get(0);
    }

    public static List<LispObject> cdr(@NotNull List<LispObject> list) {
        if (list.isEmpty()) return Collections.emptyList();
        if (list.size() == 1) return Collections.emptyList();

        return list.subList(1, list.size());
    }

    public static void checkArity(@NotNull List<LispObject> list, int expectedArity) {
        if (list.size() != expectedArity) {
            throw new LispEvaluationException("Expected arity " + expectedArity + " but got " + list.size());
        }
    }

    public static <T extends LispObject> T checkType(@NotNull LispObject lispObject, @NotNull Class<T> expectedType) {
        if (!expectedType.isAssignableFrom(lispObject.getClass())) {
            throw new LispEvaluationException("Expected type " + expectedType + " but got " + lispObject.getClass());
        }
        return expectedType.cast(lispObject);
    }

}
