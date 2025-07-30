package io.github.sakurawald.fuji.module.initializer.world.manager.structure.util;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

public class FilteredRegistry<T> extends SimpleRegistry<T> {

    private final @NotNull Registry<T> source;
    private final Predicate<T> filter;

    public FilteredRegistry(@NotNull Registry<T> source, Predicate<T> filter) {
        super(source.getKey(), source.getLifecycle());
        this.source = source;
        this.filter = filter;
    }

    /*
     *
     * The function is `streamEntries()` is used in `new DimensionOptionsRegistryHolder()`
     *
     * public DimensionOptionsRegistryHolder(Registry<DimensionOptions> registry) {
     *    this((Map)registry.streamEntries().collect(Collectors.toMap(RegistryEntry.Reference::registryKey, RegistryEntry.Reference::comp_349)));
     * }
     *
     */
    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return this.source.streamEntries().filter((e) -> this.filter.test(e.value));
    }


//    @Override
//    public Stream<RegistryEntry.Reference<T>> streamEntries() {
//        return this.source.streamEntries().filter((e) -> this.filter.test(e.value()));
//    }

    public Registry<T> getSource() {
        return this.source;
    }

    @Nullable
    @Override
    public Identifier getId(T value) {
        return filter.test(value) ? this.source.getId(value) : null;
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T entry) {
        return filter.test(entry) ? this.source.getKey(entry) : Optional.empty();
    }

    @Override
    public int getRawId(@Nullable T value) {
        return filter.test(value) ? this.source.getRawId(value) : -1;
    }

    @Nullable
    @Override
    public T get(int index) {
        return this.source.get(index);
    }

    @Override
    public int size() {
        return this.source.size();
    }

    @Nullable
    @Override
    public T get(@Nullable RegistryKey<T> key) {
        return this.source.get(key);
    }

    @Nullable
    @Override
    public T get(@Nullable Identifier id) {
        return this.source.get(id);
    }

    #if MC_VER <= MC_1_20_4
    @Override
    public Lifecycle getEntryLifecycle(T entry) {
        return this.source.getEntryLifecycle(entry);
    }
    #endif

    @Override
    public Lifecycle getLifecycle() {
        return this.source.getLifecycle();
    }

    @Override
    public Set<Identifier> getIds() {
        return this.source.getIds();
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        Set<Map.Entry<RegistryKey<T>, T>> set = new HashSet<>();
        for (Map.Entry<RegistryKey<T>, T> e : this.source.getEntrySet()) {
            try {
                T value = e.getValue();
                if (value == null) {
                    continue;
                }

                if (this.filter.test(value)) {
                    set.add(e);
                }

            } catch (Exception ignore) {
                /*
                 NOTE: The call to e.getValue() may throw exception
                 java.lang.IllegalStateException: Trying to access unbound value 'ResourceKey[minecraft:dimension / fuji:1]' from registry net.minecraft.registry.SimpleRegistry$1@5b2b172d
                 */
            }

        }
        return set;
    }


    @NotNull
    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.source.iterator(), this.filter::test);
    }

}
