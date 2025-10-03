package mod.fuji.evaluator.auxiliary;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CollectionUtil {

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static <T> T getLast(@NotNull List<T> list) {
        return list.get(list.size() - 1);
    }
}
