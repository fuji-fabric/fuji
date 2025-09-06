package io.github.sakurawald.fuji.module.mixin.chat.style;

import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.module.initializer.chat.style.ChatStyleInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryLoader;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

#if MC_VER <= MC_1_20_4
import com.mojang.datafixers.util.Pair;
import net.minecraft.resource.ResourceManager;
#elif MC_VER > MC_1_20_4 && MC_VER <= MC_1_21
import org.spongepowered.asm.mixin.injection.Coerce;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNull;
#elif MC_VER > MC_1_21
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
#endif

@SuppressWarnings({"unchecked"})
@Cite("https://github.com/Patbox/StyledChat")
@Mixin(value = RegistryLoader.class)
public class RegistryLoaderMixin {

    #if MC_VER <= MC_1_20_4
    @SuppressWarnings("LocalCaptureFailException")
    @Inject(method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/DynamicRegistryManager;Ljava/util/List;)Lnet/minecraft/registry/DynamicRegistryManager$Immutable;"
        , at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void registerNewMessageType(
        ResourceManager resourceManager
        , DynamicRegistryManager dynamicRegistryManager
        , List<RegistryLoader.Entry<?>> entries
        , CallbackInfoReturnable<DynamicRegistryManager.Immutable> cir
        , Map map
        , List<Pair<MutableRegistry<?>, Object>> iterable
    )
    #elif MC_VER > MC_1_20_4 && MC_VER <= MC_1_21
    @Inject(method = "load(Lnet/minecraft/registry/RegistryLoader$RegistryLoadable;Lnet/minecraft/registry/DynamicRegistryManager;Ljava/util/List;)Lnet/minecraft/registry/DynamicRegistryManager$Immutable;"
        , at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER))
    private static void registerNewMessageType(@Coerce Object registryLoadable
        , DynamicRegistryManager dynamicRegistryManager
        , List<RegistryLoader.Entry<?>> entries
        , CallbackInfoReturnable<DynamicRegistryManager.Immutable> cir
        , @Local(ordinal = 1) @NotNull List<RegistryLoader.Loader<?>> iterable)
    #elif MC_VER > MC_1_21
    @Inject(method = "load"
        , at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void registerNewMessageType(@Coerce Object registryLoadable
        , List<RegistryWrapper.Impl<?>> list
        , List<RegistryLoader.Entry<?>> entries
        , CallbackInfoReturnable<DynamicRegistryManager.Immutable> cir
        , Map map
        , List<RegistryLoader.Loader<?>> iterable
        , RegistryOps.RegistryInfoGetter registryInfoGetter
    )
    #endif
    {
        for (var loader : iterable) {
            #if MC_VER <= MC_1_20_4
            MutableRegistry<?> registry = loader.getFirst();
            #elif MC_VER > MC_1_20_4
            MutableRegistry<?> registry = loader.comp_2246();
            #endif

            /* Register out custom message type in the Registry<MessageType> instance. */
            RegistryKey<? extends Registry<?>> registryKey = registry.getKey();
            if (registryKey.equals(RegistryKeys.MESSAGE_TYPE)) {
                Registry<MessageType> messageTypeRegistry = (Registry<MessageType>) registry;

                // NOTE: in single-player world, the MESSAGE_TYPE_KEY will be registered twice, causing a `network protocol error` while join the world.
                if (!messageTypeRegistry.contains(ChatStyleInitializer.MESSAGE_TYPE_KEY)) {
                    Registry.register(messageTypeRegistry, ChatStyleInitializer.MESSAGE_TYPE_KEY, ChatStyleInitializer.MESSAGE_TYPE_VALUE);
                }
            }
        }
    }
}
