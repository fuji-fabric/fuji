package mod.fuji.core.auxiliary;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class MapUtil {

    public static <K, V> void renameKey(@NotNull Map<K, V> map, @NotNull K oldKey, @NotNull K newKey) {
        V remove = map.remove(oldKey);
        map.put(newKey, remove);
    }

}
