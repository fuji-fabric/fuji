package mod.fuji.core.extension;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface SimpleRegistryExtension<T> {

    @SuppressWarnings("unchecked")
    static <T> boolean remove(@NotNull MappedRegistry<T> registry, ResourceLocation key) {
        return ((SimpleRegistryExtension<T>) registry).fuji$remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(@NotNull MappedRegistry<T> registry, T value) {
        return ((SimpleRegistryExtension<T>) registry).fuji$remove(value);
    }

    boolean fuji$remove(T value);

    boolean fuji$remove(ResourceLocation key);

    void fuji$setFrozen(boolean value);

    boolean fuji$isFrozen();
}
