package mod.fuji.module.mixin.chat.style;

import mod.fuji.core.document.annotation.Cite;
import mod.fuji.module.initializer.chat.style.ChatStyleInitializer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

#if MC_VER <= MC_1_20_4
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.packs.resources.ResourceManager;
#elif MC_VER > MC_1_20_4 && MC_VER <= MC_1_21
import org.spongepowered.asm.mixin.injection.Coerce;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
#elif MC_VER > MC_1_21 && MC_VER < MC_26_1
import org.spongepowered.asm.mixin.injection.Coerce;
import net.minecraft.core.HolderLookup;
#elif MC_VER >= MC_26_1
import org.spongepowered.asm.mixin.Unique;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.concurrent.CompletableFuture;
import net.minecraft.resources.RegistryLoadTask;
#endif

@SuppressWarnings({"unchecked"})
@Cite("https://github.com/Patbox/StyledChat")
@Mixin(value = RegistryDataLoader.class)
public class RegistryLoaderMixin {

    #if MC_VER <= MC_1_20_4
    @SuppressWarnings("LocalCaptureFailException")
    @Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void registerNewMessageType(
        ResourceManager resourceManager
        , RegistryAccess dynamicRegistryManager
        , List<RegistryDataLoader.RegistryData<?>> entries
        , CallbackInfoReturnable<RegistryAccess.Frozen> cir
        , Map map
        , List<Pair<WritableRegistry<?>, Object>> iterable
    )
    #elif MC_VER > MC_1_20_4 && MC_VER <= MC_1_21
    @Inject(method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;"
        , at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER))
    private static void registerNewMessageType(@Coerce Object registryLoadable
        , RegistryAccess dynamicRegistryManager
        , List<RegistryDataLoader.RegistryData<?>> entries
        , CallbackInfoReturnable<RegistryAccess.Frozen> cir
        , @Local(ordinal = 1) @NotNull List<RegistryDataLoader.Loader<?>> iterable)
    #elif MC_VER > MC_1_21 && MC_VER < MC_26_1
    @Inject(method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;"
        , at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void registerNewMessageType(@Coerce Object registryLoadable
        , List<HolderLookup.RegistryLookup<?>> list
        , List<RegistryDataLoader.RegistryData<?>> entries
        , CallbackInfoReturnable<RegistryAccess.Frozen> cir
        , Map map
        , List<RegistryDataLoader.Loader<?>> iterable
        , RegistryOps.RegistryInfoLookup registryInfoGetter
    )
    #elif MC_VER >= MC_26_1
    @Inject(method = "lambda$load$0"
        , at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryDataLoader;createContext(Ljava/util/List;Ljava/util/List;)Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;"))
    private static void registerNewMessageType(
        CallbackInfoReturnable<CompletableFuture<RegistryAccess.Frozen>> cir,
        @Local(ordinal = 2) List<RegistryLoadTask<?>> iterable
    )
    #endif
    {
        for (var loader : iterable) {
            #if MC_VER <= MC_1_20_4
            WritableRegistry<?> registry = loader.getFirst();
            #elif MC_VER > MC_1_20_4 && MC_VER < MC_26_1
            WritableRegistry<?> registry = loader.registry();
            #elif MC_VER >= MC_26_1
            WritableRegistry<?> registry = loader.registry;
            #endif

            tryPatchRegistry(registry);
        }
    }

    @Unique
    private static void tryPatchRegistry(WritableRegistry<?> registry) {
        /* Register out custom message type in the Registry<MessageType> instance. */
        ResourceKey<? extends Registry<?>> registryKey = registry.key();
        if (registryKey.equals(Registries.CHAT_TYPE)) {
            Registry<ChatType> messageTypeRegistry = (Registry<ChatType>) registry;

            // NOTE: in single-player world, the MESSAGE_TYPE_KEY will be registered twice, causing a `network protocol error` while join the world.
            if (!messageTypeRegistry.containsKey(ChatStyleInitializer.MESSAGE_TYPE_KEY)) {
                Registry.register(messageTypeRegistry, ChatStyleInitializer.MESSAGE_TYPE_KEY, ChatStyleInitializer.MESSAGE_TYPE_VALUE);
            }
        }
    }
}
