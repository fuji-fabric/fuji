package mod.fuji.module.initializer.evaluator.evaluator.auxliary;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispFunctions {

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static @NotNull LispObject car(@NotNull LispList list) {
        List<LispObject> objects = list.getObjects();
        if (objects.isEmpty()) return Environment.NIL;
        return objects.get(0);
    }

    public static LispList cdr(@NotNull LispList list) {
        List<LispObject> objects = list.getObjects();
        if (objects.isEmpty()) return LispList.of();
        if (objects.size() == 1) return LispList.of();

        return LispList.of(objects.subList(1, objects.size()));
    }

    public static void checkArity(@NotNull LispList list, int expectedArity) {
        int size = list.getObjects().size();
        if (size != expectedArity) {
            throw new LispEvaluationException("Expected arity " + expectedArity + " but got " + size);
        }
    }

    public static <T extends LispObject> T checkType(@NotNull LispObject lispObject, @NotNull Class<T> expectedType) {
        if (!expectedType.isAssignableFrom(lispObject.getClass())) {
            throw new LispEvaluationException("Expected type " + expectedType + " but got " + lispObject.getClass());
        }
        return expectedType.cast(lispObject);
    }

}
