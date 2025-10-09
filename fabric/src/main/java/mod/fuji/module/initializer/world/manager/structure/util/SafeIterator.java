package mod.fuji.module.initializer.world.manager.structure.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class SafeIterator<T> implements Iterator<T> {
    private final Object @NotNull [] values;
    private int index = 0;

    public SafeIterator(@NotNull Iterator<T> source) {
        List<T> list = new ArrayList<>();
        source.forEachRemaining(list::add);
        this.values = list.toArray();
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
