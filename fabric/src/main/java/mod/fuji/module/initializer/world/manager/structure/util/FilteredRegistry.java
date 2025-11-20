package mod.fuji.module.initializer.world.manager.structure.util;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

public class FilteredRegistry<T> extends MappedRegistry<T> {

    private final @NotNull Registry<T> source;
    private final Predicate<T> filter;

    public FilteredRegistry(@NotNull Registry<T> source, Predicate<T> filter) {
        super(source.key(), source.registryLifecycle());
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
    public @NotNull Stream<Holder.Reference<T>>
    #if MC_VER <= MC_1_21
    holders
    #elif MC_VER > MC_1_21
    listElements
    #endif
    () {
        return RegistryHelper
            .listElements(this.source)
            .filter((e) -> this.filter.test(e.value));
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
    public ResourceLocation getKey(T value) {
        return filter.test(value) ? this.source.getKey(value) : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T entry) {
        return filter.test(entry) ? this.source.getResourceKey(entry) : Optional.empty();
    }

    @Override
    public int getId(@Nullable T value) {
        return filter.test(value) ? this.source.getId(value) : -1;
    }

    @Nullable
    @Override
    public T byId(int index) {
        return this.source.byId(index);
    }

    @Override
    public int size() {
        return this.source.size();
    }

    @Nullable
    @Override
    public T
    #if MC_VER <= MC_1_21
    get
    #elif MC_VER > MC_1_21
    getValue
    #endif
    (@Nullable ResourceKey<T> key) {
        return RegistryHelper.getValue(this.source, key);
    }

    @Nullable
    @Override
    public T
    #if MC_VER <= MC_1_21
    get
    #elif MC_VER > MC_1_21
    getValue
    #endif
    (@Nullable ResourceLocation id) {
        return RegistryHelper.getValue(this.source, id);
    }

    #if MC_VER <= MC_1_20_4
    @Override
    public Lifecycle getEntryLifecycle(T entry) {
        return this.source.getEntryLifecycle(entry);
    }
    #endif

    @Override
    public Lifecycle registryLifecycle() {
        return this.source.registryLifecycle();
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return this.source.keySet();
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        Set<Map.Entry<ResourceKey<T>, T>> set = new HashSet<>();
        for (Map.Entry<ResourceKey<T>, T> e : this.source.entrySet()) {
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
