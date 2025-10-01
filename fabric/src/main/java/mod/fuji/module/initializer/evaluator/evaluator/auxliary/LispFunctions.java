package mod.fuji.module.initializer.evaluator.evaluator.auxliary;

import java.util.List;
import java.util.function.Function;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import org.jetbrains.annotations.NotNull;

public class LispFunctions {

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static @NotNull LispObject car(@NotNull LispList list) {
        List<LispObject> objects = list.getObjects();
        if (objects.isEmpty()) return Environment.NIL;
        return objects.get(0);
    }

    public static LispList cdr(@NotNull LispList list) {
        // FIXME: value-copy and ref-copy.
        List<LispObject> objects = list.getObjects();
        if (objects.isEmpty()) return LispList.of();
        if (objects.size() == 1) return LispList.of();

        return LispList.of(objects.subList(1, objects.size()));
    }

    public static void checkArity(@NotNull LispList list, int expectedArity) {
        int size = list.size();
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

    public static void checkConstantVariableMutation(@NotNull LispSymbol lispSymbol) {
        if (lispSymbol.isConstantVariableValue()) {
            throw new LispEvaluationException("Cannot proclaim a CONSTANT variable: %s".formatted(lispSymbol.getName()));
        }
    }

    public static <T> T withCheckedVariableMutation(@NotNull Environment environment, @NotNull LispList arguments, @NotNull Function<LispSymbol, T> function) {
        LispFunctions.checkArity(arguments, 2);

        LispObject first = arguments.get(0);
        LispSymbol nameSymbol = LispFunctions.checkType(first, LispSymbol.class);

        LispSymbol lookupSymbol = environment.lookupSymbol(nameSymbol.getName());
        LispFunctions.checkConstantVariableMutation(lookupSymbol);

        return function.apply(nameSymbol);
    }

}
