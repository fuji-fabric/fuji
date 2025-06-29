package io.github.sakurawald.fuji.core.auxiliary;


import java.util.List;

public class CollectionUtil {
    public static <T> boolean validIndex(int index, List<T> list) {
        return index >= 0 && index < list.size();
    }
}
