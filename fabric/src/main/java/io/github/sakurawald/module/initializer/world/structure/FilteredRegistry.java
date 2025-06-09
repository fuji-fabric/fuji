package io.github.sakurawald.module.initializer.world.structure;

import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Stream;

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

}
