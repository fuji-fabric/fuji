package mod.fuji.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class LispString extends LispObject {

    String value;

    @Override
    public @NotNull LispObject eval(@NotNull LispEnvironment environment) {
        return this;
    }

}
