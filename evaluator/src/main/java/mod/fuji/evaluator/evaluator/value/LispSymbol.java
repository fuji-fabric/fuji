package mod.fuji.evaluator.evaluator.value;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.value.function.LispFunction;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class LispSymbol extends LispObject {

    @EqualsAndHashCode.Include String name;

    // FIXME: make LispSymbol stateless.

    Optional<LispObject> variableValue;
    Optional<LispFunction> functionValue;
    boolean isConstantVariableValue;


    public static @NotNull LispSymbol of(@NotNull String name) {
        return new LispSymbol(name, Optional.empty(), Optional.empty(), false);
    }

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        // NOTE: De-reference a symbol is to get its variable value.
        return environment
            .lookupSymbolByName(this.getName())
            .getVariableValue()
            .orElseThrow(() -> new LispEvaluationException("The variable %s is unbound.".formatted(this.name)));
    }
}
