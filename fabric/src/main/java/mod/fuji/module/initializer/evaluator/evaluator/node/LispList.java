package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LispList extends LispObject {

    final List<LispObject> nodes;

    private LispList(@NotNull List<LispObject> nodes) {
        this.nodes = nodes;
    }

    public static @NotNull LispList of() {
        return new LispList(new ArrayList<>());
    }

    public static @NotNull LispList of(LispObject... nodes) {
        List<LispObject> list = Arrays.stream(nodes).toList();
        return new LispList(list);
    }

    public static @NotNull LispList of(@NotNull List<LispObject> nodes) {
        return new LispList(nodes);
    }

    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        // FIXME
        return null;
    }

}
