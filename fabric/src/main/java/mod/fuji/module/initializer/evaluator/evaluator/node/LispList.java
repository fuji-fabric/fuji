package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.module.initializer.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.exception.LispCompilationException;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.macro.LispMacro;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form.LispSpecialForm;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.LispStandardFunction;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LispList extends LispObject {

    final List<LispObject> nodes;

    private LispList(@NotNull List<LispObject> nodes) {
        this.nodes = nodes;
    }

    public static @NotNull LispList of() {
        return new LispList(new ArrayList<>());
    }

    public static @NotNull LispList of(LispObject... nodes) {
        List<LispObject> list = Arrays.stream(nodes).toList();
        return new LispList(list);
    }

    public static @NotNull LispList of(@NotNull List<LispObject> nodes) {
        return new LispList(nodes);
    }

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        /* An empty list is treated as nil value. */
        if (this.nodes.isEmpty()) {
            return Environment.NIL;
        }

        /* The first component of a function call must be a LispSymbol. */
        LispObject first = LispFunctions.car(this.nodes);
        if (!(first instanceof LispSymbol functionNameSymbol)) {
            throw new LispCompilationException("Illegal function call.");
        }
        List<LispObject> rest = LispFunctions.cdr(this.nodes);

        /* Call the function. */
        @NotNull LispObject functionReturnValue;

        if (LispSpecialForm.isSpecialForm(functionNameSymbol)) {
            functionReturnValue = LispSpecialForm.funcall(functionNameSymbol, environment, rest);
        } else if (LispMacro.isMacro(functionNameSymbol)) {
            throw new UnsupportedOperationException();
        } else {
            functionReturnValue = LispStandardFunction.funcall(functionNameSymbol, environment, rest);
        }

        return functionReturnValue;
    }

}
