package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import org.jetbrains.annotations.NotNull;

public abstract class LispFunction extends LispObject {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    public abstract @NotNull LispObject call(@NotNull Environment environment, @NotNull List<LispObject> arguments);

}
