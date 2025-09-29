package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class LispString extends LispObject {

    String value;

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

}
