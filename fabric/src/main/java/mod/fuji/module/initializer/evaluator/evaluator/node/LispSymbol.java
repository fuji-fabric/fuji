package mod.fuji.module.initializer.evaluator.evaluator.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class LispSymbol extends LispObject {

    String name;

    public static @NotNull LispSymbol of(@NotNull String name) {
        return new LispSymbol(name);
    }

    @Override
    public @NotNull LispObject eval() {
        return this;
    }
}
