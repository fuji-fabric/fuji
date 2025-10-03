package mod.fuji.module.initializer.evaluator.evaluator.auxliary;

import java.util.List;
import java.util.function.Function;
import mod.fuji.module.initializer.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
public class LispFunctions {

    public static @NotNull LispObject car(@NotNull LispList list) {
        List<LispObject> objects = list.getObjects();
        if (objects.isEmpty()) return LispEnvironment.NIL;
        return objects.get(0);
    }

    public static LispList cdr(@NotNull LispList list) {
        // FIXME: value-copy and ref-copy.
        List<LispObject> objects = list.getObjects();
        if (objects.isEmpty()) return LispList.of();
        if (objects.size() == 1) return LispList.of();

        return LispList.of(objects.subList(1, objects.size()));
    }

    public static void checkExactlyArity(@NotNull LispList list, int expectedArity) {
        int arity = list.size();
        if (arity != expectedArity) {
            throw new LispEvaluationException("Expected arity " + expectedArity + " but got " + arity);
        }
    }

    public static void checkRequiredArity(@NotNull LispList list, int requiredArity) {
        int arity = list.size();
        if (arity < requiredArity) {
            throw new LispEvaluationException("""
                Too few elements in
                  %s
                at least %d expected, but got %d
                """.formatted(list, requiredArity, arity));
        }
    }

    public static <T extends LispObject> T checkType(@NotNull LispObject lispObject, @NotNull Class<T> expectedType) {
        if (!expectedType.isAssignableFrom(lispObject.getClass())) {
            throw new LispEvaluationException("Expected type " + expectedType + " but got " + lispObject.getClass());
        }
        return expectedType.cast(lispObject);
    }

    public static <T> T withCheckedVariableMutation(@NotNull LispEnvironment environment, @NotNull LispList arguments, @NotNull Function<LispSymbol, T> function) {
        LispFunctions.checkExactlyArity(arguments, 2);

        LispObject first = arguments.get(0);
        LispSymbol nameSymbol = LispFunctions.checkType(first, LispSymbol.class);

        LispSymbol lookupSymbol = environment.lookupSymbolByName(nameSymbol.getName());

        return function.apply(lookupSymbol);
    }


    public static @NotNull LispList evalForms(@NotNull LispEnvironment environment, @NotNull LispList forms) {
        LispList values = LispList.of();
        for (LispObject form : forms) {
            LispObject value = form.eval(environment);
            values.add(value);
        }
        return values;
    }
}
