package mod.fuji.module.initializer.evaluator.evaluator.node;


import java.util.List;
import java.util.Set;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import org.jetbrains.annotations.NotNull;

public class LispSpecialForm extends LispFunction {

    private static final Set<String> RESERVED_SPECIAL_FORM_NAMES = Set.of(
        "block", "if", "progv", "catch", "labels", "quote", "let", "let*",
        "return-from", "declare", "setq", "eval-when", "macrolet", "tagbody",
        "flet", "multiple_value-call", "multiple-value-prog1", "the", "function",
        "throw", "go", "progn", "unwind-protect", "compiler-let"
    );

    public static boolean isSpecialForm(@NotNull LispSymbol lispSymbol) {
        return RESERVED_SPECIAL_FORM_NAMES.contains(lispSymbol.getName());
    }

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    @Override
    public @NotNull LispObject apply(@NotNull Environment environment, @NotNull List<LispObject> arguments) {
        return this;
    }
}
