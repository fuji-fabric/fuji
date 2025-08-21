package io.github.sakurawald.fuji.core.structure;

import lombok.Value;

@Value
public class Triple<A, B, C> {
    A first;
    B second;
    C third;
}
