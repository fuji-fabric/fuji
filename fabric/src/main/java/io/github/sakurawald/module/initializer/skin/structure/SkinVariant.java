package io.github.sakurawald.module.initializer.skin.structure;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SkinVariant {

    CLASSIC("classic"),
    SLIM("slim");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
