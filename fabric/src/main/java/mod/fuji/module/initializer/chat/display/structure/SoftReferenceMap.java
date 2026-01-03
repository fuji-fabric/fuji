package mod.fuji.module.initializer.chat.display.structure;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class SoftReferenceMap<K, V> {

    private final Map<K, SoftReference<V>> backendMap = new HashMap<>();

    public void put(K key, V value) {
        SoftReference<V> softRef = new SoftReference<>(value);
        backendMap.put(key, softRef);
    }

    public @Nullable V get(K key) {
        SoftReference<V> softRef = backendMap.get(key);
        if (softRef != null) {
            return softRef.get();
        }
        return null;
    }

    public boolean containsKey(K key) {
        return backendMap.containsKey(key);
    }

    public void remove(K key) {
        backendMap.remove(key);
    }

    public void clear() {
        backendMap.clear();
    }
}
