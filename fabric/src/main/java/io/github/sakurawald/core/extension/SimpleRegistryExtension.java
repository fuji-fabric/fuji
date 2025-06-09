package io.github.sakurawald.core.extension;

import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface SimpleRegistryExtension<T> {

    @SuppressWarnings("unchecked")
    static <T> boolean remove(@NotNull SimpleRegistry<T> registry, Identifier key) {
        return ((SimpleRegistryExtension<T>) registry).fuji$remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(@NotNull SimpleRegistry<T> registry, T value) {
        return ((SimpleRegistryExtension<T>) registry).fuji$remove(value);
    }

    boolean fuji$remove(T value);

    boolean fuji$remove(Identifier key);

    void fuji$setFrozen(boolean value);

    boolean fuji$isFrozen();
}
