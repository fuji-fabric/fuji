package mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form;


import java.util.List;
import java.util.Set;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.LispFunction;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("""
    For Lisp special forms, refer to the `SB-C` and `ir1-translators.lisp` file.
    """)
public abstract class LispSpecialForm extends LispFunction {

    private static final Set<String> RESERVED_SPECIAL_FORM_NAMES = Set.of(
        "block", "if", "progv", "catch", "labels", "quote", "let", "let*",
        "return-from", "declare", "setq", "eval-when", "macrolet", "tagbody",
        "flet", "multiple_value-call", "multiple-value-prog1", "the", "function",
        "throw", "go", "progn", "unwind-protect", "compiler-let", "defvar"
    );

    public static boolean isSpecialForm(@NotNull LispSymbol lispSymbol) {
        return RESERVED_SPECIAL_FORM_NAMES.contains(lispSymbol.getName());
    }

    public static @NotNull LispObject funcall(@NotNull LispSymbol functionNameSymbol, @NotNull Environment environment, @NotNull List<LispObject> arguments) {
        LispFunction functionValue = environment
            .lookupSymbol(functionNameSymbol.getName())
            .getFunctionValue()
            .orElseThrow(() -> new LispEvaluationException("The function %s is undefined.".formatted(functionNameSymbol.getName())));

        return functionValue.apply(environment, arguments);
    }

}
