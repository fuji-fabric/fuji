package mod.fuji.evaluator.evaluator.context;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import mod.fuji.evaluator.evaluator.value.LispObject;
import mod.fuji.evaluator.evaluator.value.LispString;
import mod.fuji.evaluator.evaluator.value.LispSymbol;
import mod.fuji.evaluator.evaluator.value.function.LispFunction;
import mod.fuji.evaluator.evaluator.value.function.kinds.special_form.LispDefconstant;
import mod.fuji.evaluator.evaluator.value.function.kinds.special_form.LispDefparameter;
import mod.fuji.evaluator.evaluator.value.function.kinds.special_form.LispDefvar;
import mod.fuji.evaluator.evaluator.value.function.kinds.special_form.LispProgn;
import mod.fuji.evaluator.evaluator.value.function.kinds.standard.builtin.AdditionFunction;
import mod.fuji.evaluator.evaluator.value.function.kinds.standard.builtin.DivideFunction;
import mod.fuji.evaluator.evaluator.value.function.kinds.standard.builtin.MultiplyFunction;
import mod.fuji.evaluator.evaluator.value.function.kinds.standard.builtin.SubtractFunction;
import org.jetbrains.annotations.NotNull;

@Value
@AllArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public class LispEnvironment {

    public static final LispSymbol NIL = LispSymbol.of("nil");
    public static final LispSymbol T = LispSymbol.of("t");

    @NotNull Optional<LispEnvironment> parent;
    @NotNull Map<String, LispSymbol> symbols;

    public static @NotNull LispEnvironment ofNullLexical() {
        /* Make an empty environment. */
        LispEnvironment environment = new LispEnvironment(Optional.empty(), new HashMap<>());

        /* Define the things that's known in fndb. */
        environment.setFunctionValue(LispSymbol.of("+"), new AdditionFunction());
        environment.setFunctionValue(LispSymbol.of("*"), new MultiplyFunction());
        environment.setFunctionValue(LispSymbol.of("-"), new SubtractFunction());
        environment.setFunctionValue(LispSymbol.of("/"), new DivideFunction());
        environment.setFunctionValue(LispSymbol.of("progn"), new LispProgn());
        environment.setFunctionValue(LispSymbol.of("defvar"), new LispDefvar());
        environment.setFunctionValue(LispSymbol.of("defparameter"), new LispDefparameter());
        environment.setFunctionValue(LispSymbol.of("defconstant"), new LispDefconstant());
        environment.setVariableValue(LispSymbol.of("*test-version*"), LispString.of("1.0.0"));

        environment.setNamedConstant(NIL, NIL);
        environment.setNamedConstant(T, T);
        return environment;
    }

    /**
     * For simplicity, the <code>named constant</code> entity is type of <code>variable</code>.
     * Here we use one bit to mark a variable as a named constant.
     * When the value of a <code>named constant</code> is <code>altered</code> or <code>re-bound</code>, an error should be signaled.
     */
    public void setNamedConstant(@NotNull LispSymbol lispSymbol, @NotNull LispObject value) {
        LispSymbol lookup = this.setVariableValue(lispSymbol, value);
        lookup.setConstantVariableValue(true);
    }

    @CanIgnoreReturnValue
    public @NotNull LispSymbol setFunctionValue(@NotNull LispSymbol symbol, @NotNull LispFunction function) {
        LispSymbol lispSymbol = internSymbol(symbol.getName());
        lispSymbol.setFunctionValue(Optional.of(function));
        return lispSymbol;
    }

    @CanIgnoreReturnValue
    public @NotNull LispSymbol setVariableValue(@NotNull LispSymbol symbol, @NotNull LispObject variableValue) {
        LispSymbol lispSymbol = internSymbol(symbol.getName());
        lispSymbol.setVariableValue(Optional.of(variableValue));
        return lispSymbol;
    }

    private @NotNull LispSymbol internSymbol(@NotNull String symbolName) {
        return Optional
            .ofNullable(symbols.get(symbolName))
            .orElseGet(() -> {
                LispSymbol newValue = LispSymbol.of(symbolName);
                symbols.put(symbolName, newValue);
                return newValue;
            });
    }

    public @NotNull LispSymbol lookupSymbolByName(@NotNull String symbolName) {
        // FIXME: proper symbol shadowing
        return internSymbol(symbolName);
    }

}
