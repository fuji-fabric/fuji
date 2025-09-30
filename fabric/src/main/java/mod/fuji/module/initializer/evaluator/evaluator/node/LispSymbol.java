package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.function.LispFunction;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class LispSymbol extends LispObject {

    String name;

    Optional<LispObject> variableValue;
    Optional<LispFunction> functionValue;

    public static @NotNull LispSymbol of(@NotNull String name) {
        return new LispSymbol(name, Optional.empty(), Optional.empty());
    }

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        // NOTE: De-reference a symbol is to get its variable value.
        return environment
            .lookupSymbol(this.getName())
            .getVariableValue()
            .orElseThrow(() -> new LispEvaluationException("The variable %s is unbound.".formatted(this.name)));
    }
}
