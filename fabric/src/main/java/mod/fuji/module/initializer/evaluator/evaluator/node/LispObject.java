package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.Data;
import mod.fuji.module.initializer.evaluator.evaluator.context.LispEnvironment;
import org.jetbrains.annotations.NotNull;

@Data
public abstract class LispObject {

    public abstract @NotNull LispObject eval(@NotNull LispEnvironment environment);

}
