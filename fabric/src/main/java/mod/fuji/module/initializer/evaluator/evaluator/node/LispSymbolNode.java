package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class LispSymbolNode extends LispNode {

    String name;

    public static @NotNull LispSymbolNode of(@NotNull String name) {
        return new LispSymbolNode(name);
    }

}
