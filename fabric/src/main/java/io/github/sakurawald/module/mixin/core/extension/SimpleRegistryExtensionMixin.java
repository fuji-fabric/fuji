package io.github.sakurawald.module.mixin.core.extension;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.extension.SimpleRegistryExtension;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryExtensionMixin<T> implements SimpleRegistryExtension<T>, MutableRegistry<T> {

    @Shadow
    @Final
    private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Shadow
    @Final
    private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Shadow
    @Final
    private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

    @Shadow
    @Final
    private Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo;

    @Shadow
    @Final
    private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Shadow
    @Final
    private Reference2IntMap<T> entryToRawId;

    @Shadow
    @Final
    RegistryKey<? extends Registry<T>> key;

    @Shadow
    private boolean frozen;


    @Override
    public boolean fuji$remove(@NotNull T entry) {
        var registryEntry = this.valueToEntry.get(entry);
        int rawId = this.entryToRawId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        try {
            this.keyToEntry.remove(registryEntry.registryKey());
            this.idToEntry.remove(registryEntry.registryKey().getValue());
            this.valueToEntry.remove(entry);
            this.rawIdToEntry.set(rawId, null);
            this.keyToEntryInfo.remove(this.key);
            return true;
        } catch (Throwable e) {
            LogUtil.error("failed to remove entry: {}", entry.toString());
            return false;
        }
    }

    @Override
    public boolean fuji$remove(Identifier key) {
        var entry = this.idToEntry.get(key);
        return entry != null && entry.hasKeyAndValue() && this.fuji$remove(entry.comp_349());
    }

    @Override
    public void fuji$setFrozen(boolean value) {
        this.frozen = value;
    }

    @Override
    public boolean fuji$isFrozen() {
        return this.frozen;
    }

    @ModifyReturnValue(method = "streamEntries", at = @At("RETURN"))
    public Stream<Reference<T>> fixEntryStream(@NotNull Stream<RegistryEntry.Reference<T>> original) {
        return original.filter(Objects::nonNull);
    }
}

