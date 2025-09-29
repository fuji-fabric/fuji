package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class LispString extends LispObject {

    String value;

    @Override
    public @NotNull LispObject eval() {
        return this;
    }

}
