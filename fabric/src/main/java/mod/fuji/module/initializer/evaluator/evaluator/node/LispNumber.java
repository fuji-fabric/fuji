package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
    public @NotNull LispObject eval() {
        return this;
    }
}
