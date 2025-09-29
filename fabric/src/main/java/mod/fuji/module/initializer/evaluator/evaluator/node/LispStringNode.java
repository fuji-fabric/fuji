package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class LispStringNode extends LispNode {

    String value;

    @Override
    public @NotNull LispNode eval() {
        return this;
    }

}
