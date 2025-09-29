package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LispListNode extends LispNode {

    final List<LispNode> nodes;

    private LispListNode(List<LispNode> nodes) {
        this.nodes = nodes;
    }


    public static @NotNull LispListNode of() {
        return new LispListNode(new ArrayList<>());
    }

    public static @NotNull LispListNode of(LispNode... nodes) {
        return new LispListNode(Arrays.stream(nodes).toList());
    }

    public static @NotNull LispListNode of(@NotNull List<LispNode> nodes) {
        return new LispListNode(nodes);
    }

    @Override
    public @NotNull LispNode eval() {
        // FIXME
        return null;
    }

}
