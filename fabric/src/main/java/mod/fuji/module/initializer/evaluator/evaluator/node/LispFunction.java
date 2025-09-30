package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.List;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("""
    The most important kind of `eval` is `apply`.
    The most important kind of `apply` is `eval`.
    """)
public abstract class LispFunction extends LispObject {

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        return this;
    }

    public abstract @NotNull LispObject apply(@NotNull Environment environment, @NotNull List<LispObject> arguments);

}
