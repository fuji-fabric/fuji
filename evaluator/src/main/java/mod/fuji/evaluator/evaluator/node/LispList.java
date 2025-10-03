package mod.fuji.evaluator.evaluator.node;

import com.google.errorprone.annotations.Keep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import mod.fuji.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.evaluator.evaluator.compiler.exception.LispCompilationException;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.node.function.LispFunction;
import mod.fuji.evaluator.evaluator.node.function.macro.LispMacro;
import mod.fuji.evaluator.evaluator.node.function.special_form.LispSpecialForm;
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
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        /* An empty list is treated as nil value. */
        if (this.objects.isEmpty()) {
            return LispEnvironment.NIL;
        }

        /* The first component of a function call must be a LispSymbol. */
        LispObject first = LispFunctions.car(this);
        if (!(first instanceof LispSymbol functionNameSymbol)) {
            throw new LispCompilationException("Illegal function call.");
        }
        LispList args = LispFunctions.cdr(this);

        /* Call the function. */
        @NotNull LispObject functionReturnValue;

        if (LispSpecialForm.isSpecialForm(functionNameSymbol)) {
            functionReturnValue = LispFunction.funcall(functionNameSymbol, environment, args);
        } else if (LispMacro.isMacro(functionNameSymbol)) {
            functionReturnValue = LispFunction.funcall(functionNameSymbol, environment, args);
        } else {
            args = LispFunctions.evalForms(environment, args);
            functionReturnValue = LispFunction.funcall(functionNameSymbol, environment, args);
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

    public void add(LispObject element) {
        this.objects.add(element);
    }

    @Keep
    public LispList sublist(int start, int end) {
        if (start == end) return LispList.of();
        if (start >= objects.size()) return LispList.of();
        return new LispList(objects.subList(start, end));
    }

}
