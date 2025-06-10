package io.github.sakurawald.module.mixin.chat.style.message_type;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.module.initializer.chat.style.ChatStyleInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

#if MC_VER == MC_1_21
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
#endif

#if MC_VER > MC_1_21
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Map;
#endif

@SuppressWarnings({"unchecked"})
@Cite("https://github.com/Patbox/StyledChat")
@Mixin(value = RegistryLoader.class)
public class RegistryLoaderMixin {

    #if MC_VER <= MC_1_21
    @Inject(method = "load(Lnet/minecraft/registry/RegistryLoader$RegistryLoadable;Lnet/minecraft/registry/DynamicRegistryManager;Ljava/util/List;)Lnet/minecraft/registry/DynamicRegistryManager$Immutable;"
        , at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER))
    private static void registerNewMessageType(@Coerce Object registryLoadable
        , DynamicRegistryManager dynamicRegistryManager
        , List<RegistryLoader.Entry<?>> entries
        , CallbackInfoReturnable<DynamicRegistryManager.Immutable> cir
        , @Local(ordinal = 1) @NotNull List<RegistryLoader.Loader<?>> loaders)
    #elif MC_VER > MC_1_21
    @Inject(method = "load"
        , at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void registerNewMessageType(@Coerce Object registryLoadable
        , List<RegistryWrapper.Impl<?>> list
        , List<RegistryLoader.Entry<?>> entries
        , CallbackInfoReturnable<DynamicRegistryManager.Immutable> cir
        , Map map
        , List<RegistryLoader.Loader<?>> loaders
        , RegistryOps.RegistryInfoGetter registryInfoGetter
    )
    #endif
    {

        for (RegistryLoader.Loader<?> entry : loaders) {
            MutableRegistry<?> registry = entry.comp_2246();
            RegistryKey<? extends Registry<?>> registryKey = registry.getKey();

            if (registryKey.equals(RegistryKeys.MESSAGE_TYPE)) {
                Registry<MessageType> registryForMessageType = (Registry<MessageType>) registry;

                // note: in single-player world, the MESSAGE_TYPE_KEY will be registered twice, causing a `network protocol error` while join the world.
                if (!registryForMessageType.contains(ChatStyleInitializer.MESSAGE_TYPE_KEY)) {
                    Registry.register(registryForMessageType, ChatStyleInitializer.MESSAGE_TYPE_KEY, ChatStyleInitializer.MESSAGE_TYPE_VALUE);
                }
            }
        }
    }
}
