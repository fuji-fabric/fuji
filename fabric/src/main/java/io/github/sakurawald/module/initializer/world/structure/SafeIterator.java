package io.github.sakurawald.module.initializer.world.structure;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

public final class SafeIterator<T> implements Iterator<T> {
    private final Object @NotNull [] values;
    private int index = 0;

    public SafeIterator(@NotNull Collection<T> source) {
        this.values = source.toArray();
    }

    @Override
    public boolean hasNext() {
        return this.values.length > this.index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        return (T) this.values[this.index++];
    }
}
