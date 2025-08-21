package io.github.sakurawald.fuji.core.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair<K, V> {
    K key;
    V value;
}

