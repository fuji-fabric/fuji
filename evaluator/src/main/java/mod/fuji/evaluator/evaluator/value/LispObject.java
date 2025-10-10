package mod.fuji.evaluator.evaluator.value;

import lombok.Data;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import org.jetbrains.annotations.NotNull;

@Data
public abstract class LispObject {

    public abstract @NotNull LispObject eval(@NotNull LispEnvironment environment);

}
