package io.github.sakurawald.module.mixin.chat.style.message_type;

import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.module.initializer.chat.style.ChatStyleInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked"})
@Cite("https://github.com/Patbox/StyledChat")
@Mixin(value = RegistryLoader.class)
public class RegistryLoaderMixin {

    @Inject(method = "load"
        , at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void registerNewMessageType(@Coerce Object registryLoadable
        , List<RegistryWrapper.Impl<?>> list
        , List<RegistryLoader.Entry<?>> entries
        , CallbackInfoReturnable<DynamicRegistryManager.Immutable> cir
        , Map map
        , List<RegistryLoader.Loader<?>> list2
        , RegistryOps.RegistryInfoGetter registryInfoGetter
    ) {

        for (RegistryLoader.Loader<?> entry : list2) {
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
