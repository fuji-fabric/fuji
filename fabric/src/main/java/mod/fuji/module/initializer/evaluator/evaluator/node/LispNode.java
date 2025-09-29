package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public abstract class LispNode {

    public abstract @NotNull LispNode eval();

}
