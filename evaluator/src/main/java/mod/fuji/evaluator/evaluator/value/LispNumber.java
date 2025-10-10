package mod.fuji.evaluator.evaluator.value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class LispNumber extends LispObject {

    double value;

    public static @NotNull LispNumber of(double value) {
        return new LispNumber(value);
    }

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        return this;
    }
}
