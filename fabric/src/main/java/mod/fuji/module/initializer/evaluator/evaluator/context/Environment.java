package mod.fuji.module.initializer.evaluator.evaluator.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispString;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.LispFunction;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form.DefvarSpecialForm;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.special_form.PrognSpecialForm;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.builtin.AdditionFunction;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.builtin.DivideFunction;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.builtin.MultiplyFunction;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.standard.builtin.SubtractFunction;
import org.jetbrains.annotations.NotNull;

@Value
@AllArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public class Environment {

    public static final LispSymbol NIL = LispSymbol.of("nil");
    public static final LispSymbol T = LispSymbol.of("t");

    @NotNull Optional<Environment> parent;
    @NotNull Map<String, LispSymbol> symbols;

    public static @NotNull Environment ofTopLevel() {
        /* Make an empty environment. */
        Environment environment = new Environment(Optional.empty(), new HashMap<>());

        /* Define the things that's known in fndb. */
        environment.defineFunction(LispSymbol.of("+"), new AdditionFunction());
        environment.defineFunction(LispSymbol.of("*"), new MultiplyFunction());
        environment.defineFunction(LispSymbol.of("-"), new SubtractFunction());
        environment.defineFunction(LispSymbol.of("/"), new DivideFunction());
        environment.defineFunction(LispSymbol.of("defvar"), new DefvarSpecialForm());
        environment.defineFunction(LispSymbol.of("progn"), new PrognSpecialForm());
        environment.defineVariable(LispSymbol.of("*test-version*"), LispString.of("1.0.0"));

        environment.defineNamedConstant(NIL);
        environment.defineNamedConstant(T);
        return environment;
    }

    private void defineSelfEvaluatingSymbol(@NotNull LispSymbol lispSymbol) {
        this.defineVariable(lispSymbol, lispSymbol);
    }

    private void defineNamedConstant(@NotNull LispSymbol lispSymbol) {
        // FIXME: a better impl.
        this.defineSelfEvaluatingSymbol(lispSymbol);
    }

    public void defineFunction(@NotNull LispSymbol symbol, @NotNull LispFunction function) {
        internSymbol(symbol.getName())
            .setFunctionValue(Optional.of(function));
    }

    public void defineVariable(@NotNull LispSymbol symbol, @NotNull LispObject variableValue) {
        internSymbol(symbol.getName())
            .setVariableValue(Optional.of(variableValue));
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

    public @NotNull LispSymbol lookupSymbol(@NotNull String symbolName) {
        return internSymbol(symbolName);
    }

}
