package io.github.sakurawald.fuji.module.mixin.system_message;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.module.initializer.system_message.SystemMessageInitializer;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerEntity.class, priority = EventConsumer.HIGHEST)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "sendMessageToClient", at = @At("HEAD"), cancellable = true)
    void cancelText(Text text, boolean bl, CallbackInfo ci, @Local(argsOnly = true)LocalRef<Text> textRef) {
        // NOTE: The MutableText made from Text.translatable() has no siblings.
        if (!text.getSiblings().isEmpty()) return;

        /* Cancel the sending of specified translatable text to players. */
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            String translatableKey = translatableTextContent.getKey();
            Object[] translatableArgs = translatableTextContent.getArgs();

            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            Optional<MutableText> newValue = SystemMessageInitializer.modifyTranslatableText(player, translatableKey, translatableArgs);

            if (newValue.isEmpty()) {
                // NOTE: If the value is specified to null, then it means we should cancel the sending of it.
                ci.cancel();
            } else {
                textRef.set(newValue.get());
            }
        }
    }

}
