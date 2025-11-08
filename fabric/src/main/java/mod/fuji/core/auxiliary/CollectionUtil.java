package mod.fuji.core.auxiliary;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

    public static <T> Optional<Integer> findFirstIndex(@NotNull List<T> list, @NotNull Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            if (predicate.test(t)) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> lastElement(@NotNull List<T> list) {
        if (list.isEmpty()) return Optional.empty();

        return (Optional<T>) list.get(list.size() - 1);
    }

}
