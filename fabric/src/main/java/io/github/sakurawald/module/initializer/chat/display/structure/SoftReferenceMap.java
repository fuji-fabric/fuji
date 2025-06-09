package io.github.sakurawald.module.initializer.chat.display.structure;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class SoftReferenceMap<K, V> {
    private final Map<K, SoftReference<V>> map = new HashMap<>();

    public void put(K key, V value) {
        SoftReference<V> softRef = new SoftReference<>(value);
        map.put(key, softRef);
    }

    public @Nullable V get(K key) {
        SoftReference<V> softRef = map.get(key);
        if (softRef != null) {
            return softRef.get();
        }
        return null;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }
}
