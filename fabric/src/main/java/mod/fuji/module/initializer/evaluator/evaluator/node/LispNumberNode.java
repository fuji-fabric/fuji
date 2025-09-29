package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class LispNumberNode extends LispNode {

    double value;

    public static @NotNull LispNumberNode of(double value) {
        return new LispNumberNode(value);
    }

    @Override
    public @NotNull LispNode eval() {
        return this;
    }
}
