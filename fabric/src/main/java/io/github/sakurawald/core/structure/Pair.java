package io.github.sakurawald.core.structure;

import lombok.Data;

@Data
public class Pair<K, V> {
    final K key;
    final V value;
}

