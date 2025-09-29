package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class LispSymbol extends LispObject {

    String name;

    LispObject variableValue;
    LispObject functionValue;

    public static @NotNull LispSymbol of(@NotNull String name) {
        return new LispSymbol(name, null, null);
    }

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        // NOTE: De-reference a symbol is to get its variable value.
        if (this.variableValue == null) {
            throw new LispEvaluationException("The variable %s is unbound.".formatted(this.name));
        }

        return this.variableValue;
    }
}
