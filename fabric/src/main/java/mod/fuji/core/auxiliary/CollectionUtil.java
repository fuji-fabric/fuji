package mod.fuji.core.auxiliary;


import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CollectionUtil {
    public static <T> boolean validIndex(int index, List<T> list) {
        return index >= 0 && index < list.size();
    }

    public static <T> void moveElementLeft(@NotNull List<T> list, @NotNull T element) {
        int index = list.indexOf(element);
        if (index == 0) return;
        Collections.swap(list, index, index - 1);
    }

    public static <T> void moveElementRight(@NotNull List<T> list, @NotNull T element) {
        int index = list.indexOf(element);
        if (index == list.size() - 1) return;
        Collections.swap(list, index, index + 1);
    }


    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static <T> T getLast(@NotNull List<T> list) {
        return list.get(list.size() - 1);
    }

}
