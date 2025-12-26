package mod.fuji.module.initializer.world.manager.structure.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

public final class SnapshotArrayList<T> extends AbstractList<T> implements RandomAccess {

    private final Object @NotNull [] values;

    public SnapshotArrayList(@NotNull Collection<? extends T> source) {
        this.values = source.toArray();
    }

    public SnapshotArrayList(@NotNull Iterator<? extends T> source) {
        List<T> list = new ArrayList<>();
        source.forEachRemaining(list::add);
        this.values = list.toArray();
    }

    public SnapshotArrayList(@NotNull Iterable<? extends T> source) {
        this(source.iterator());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException("SafeArrayList is read-only");
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException("SafeArrayList is read-only");
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("SafeArrayList is read-only");
    }

    @Override
    public void replaceAll(@NotNull UnaryOperator<T> operator) {
        throw new UnsupportedOperationException("SafeArrayList is read-only");
    }

    @Override
    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException("SafeArrayList is read-only");
    }
}
