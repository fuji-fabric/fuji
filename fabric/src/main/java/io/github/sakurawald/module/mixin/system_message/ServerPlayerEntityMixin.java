package io.github.sakurawald.module.mixin.system_message;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.module.initializer.system_message.SystemMessageInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = ServerPlayerEntity.class, priority = 1000 - 500)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "sendMessageToClient", at = @At("HEAD"), cancellable = true)
    void cancelText(Text text, boolean bl, CallbackInfo ci) {
        // the MutableText made from Text.translatable() has no siblings.
        if (!text.getSiblings().isEmpty()) return;

        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            String translatableKey = translatableTextContent.getKey();

            Map<String, String> key2value = SystemMessageInitializer.config.model().key2value;
            if (key2value.containsKey(translatableKey)
                && key2value.get(translatableKey) == null) {

                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) (Object) this;
                LogUtil.debug("cancel sending {} to player {}", translatableKey, serverPlayerEntity.getGameProfile().getName());
                ci.cancel();
            }
        }
    }

}
