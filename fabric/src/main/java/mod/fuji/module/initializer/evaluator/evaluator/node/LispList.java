package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import mod.fuji.module.initializer.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.exception.LispCompilationException;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.macro.LispMacro;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form.LispSpecialForm;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.LispStandardFunction;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class LispList extends LispObject implements Iterable<LispObject> {

    @NotNull List<LispObject> objects;

    private LispList(@NotNull List<LispObject> objects) {
        this.objects = objects;
    }

    public static @NotNull LispList of() {
        return new LispList(new ArrayList<>());
    }

    public static @NotNull LispList of(@NotNull LispObject... objects) {
        List<LispObject> list = Arrays.stream(objects).toList();
        return new LispList(list);
    }

    public static @NotNull LispList of(@NotNull List<LispObject> objects) {
        return new LispList(objects);
    }

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        /* An empty list is treated as nil value. */
        if (this.objects.isEmpty()) {
            return Environment.NIL;
        }

        /* The first component of a function call must be a LispSymbol. */
        LispObject first = LispFunctions.car(this);
        if (!(first instanceof LispSymbol functionNameSymbol)) {
            throw new LispCompilationException("Illegal function call.");
        }
        LispList rest = LispFunctions.cdr(this);

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

    @Override
    public @NotNull Iterator<LispObject> iterator() {
        return this.objects.iterator();
    }

    public int size() {
        return objects.size();
    }

    public LispObject get(int index) {
        return this.objects.get(index);
    }

}
