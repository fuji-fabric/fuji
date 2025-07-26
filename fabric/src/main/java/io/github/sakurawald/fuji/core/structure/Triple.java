package io.github.sakurawald.fuji.core.structure;

import lombok.Data;

@Data
public class Triple<A, B, C> {
    final A first;
    final B second;
    final C third;
}
