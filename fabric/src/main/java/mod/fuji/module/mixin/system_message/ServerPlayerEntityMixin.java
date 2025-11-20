package mod.fuji.module.mixin.system_message;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.module.initializer.system_message.SystemMessageInitializer;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = EventConsumer.HIGHEST)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"), cancellable = true)
    void modifyTranslatableText(Component text, boolean bl, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Component> textRef) {
        // NOTE: The MutableText made from Text.translatable() has no siblings.
        if (!text.getSiblings().isEmpty()) return;

        if (text.getContents() instanceof TranslatableContents translatableTextContent) {
            String translatableKey = translatableTextContent.getKey();

            SystemMessageInitializer
                .findApplicableRule(translatableKey)
                .ifPresent(rule -> {
                    ServerPlayer player = (ServerPlayer) (Object) this;
                    Object[] translatableArgs = translatableTextContent.getArgs();

                    Optional<MutableComponent> newValue = SystemMessageInitializer.computeTranslatableTextResult(rule, player, translatableKey, translatableArgs);

                    if (newValue.isEmpty()) {
                        ci.cancel();
                    } else {
                        textRef.set(newValue.get());
                    }
                });

        }
    }

}
