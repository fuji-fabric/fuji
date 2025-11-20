package mod.fuji.module.mixin.core.extension;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.extension.SimpleRegistryExtension;
import it.unimi.dsi.fastutil.objects.ObjectList;
#if MC_VER <= MC_1_20_2
import it.unimi.dsi.fastutil.objects.Object2IntMap;
#elif MC_VER > MC_1_20_2
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
#endif

import net.minecraft.core.WritableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import net.minecraft.core.RegistrationInfo;
#endif
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(MappedRegistry.class)
public abstract class SimpleRegistryExtensionMixin<T> implements SimpleRegistryExtension<T>, WritableRegistry<T> {

    @Shadow
    @Final
    private Map<T, Holder.Reference<T>> byValue;

    @Shadow
    @Final
    private Map<ResourceLocation, Holder.Reference<T>> byLocation;

    @Shadow
    @Final
    private Map<ResourceKey<T>, Holder.Reference<T>> byKey;

    #if MC_VER <= MC_1_20_4
    #elif MC_VER > MC_1_20_4
    @Shadow
    @Final
    private Map<ResourceKey<T>, RegistrationInfo> registrationInfos;
    #endif


    @Shadow
    @Final
    private ObjectList<Holder.Reference<T>> byId;

    #if MC_VER <= MC_1_20_2
    @Shadow
    @Final
    private Object2IntMap<T> entryToRawId;
    #elif MC_VER > MC_1_20_2
    @Shadow
    @Final
    private Reference2IntMap<T> toId;
    #endif

    @Shadow
    @Final
    ResourceKey<? extends Registry<T>> key;

    @Shadow
    private boolean frozen;


    @Override
    public boolean fuji$remove(@NotNull T entry) {
        var registryEntry = this.byValue.get(entry);
        int rawId = this.toId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        try {
            this.byKey.remove(registryEntry.key());
            this.byLocation.remove(registryEntry.key().location());
            this.byValue.remove(entry);
            this.byId.set(rawId, null);
            #if MC_VER <= MC_1_20_4
            #elif MC_VER > MC_1_20_4
            this.registrationInfos.remove(this.key);
            #endif
            return true;
        } catch (Throwable e) {
            LogUtil.error("Failed to remove entry: {}", entry.toString(), e);
            return false;
        }
    }

    @Override
    public boolean fuji$remove(ResourceLocation key) {
        var entry = this.byLocation.get(key);
        return entry != null && entry.isBound() && this.fuji$remove(entry.value());
    }

    @Override
    public void fuji$setFrozen(boolean value) {
        this.frozen = value;
    }

    @Override
    public boolean fuji$isFrozen() {
        return this.frozen;
    }

    @ModifyReturnValue(method = "listElements", at = @At("RETURN"))
    public Stream<Reference<T>> fixEntryStream(@NotNull Stream<Holder.Reference<T>> original) {
        return original.filter(Objects::nonNull);
    }
}

