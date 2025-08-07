package io.github.sakurawald.fuji.module.mixin.chat.history;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.module.initializer.chat.history.ChatHistoryInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class StoreChatHistoryMixin {

    @ForDeveloper("""
        The injection point should be `RETURN` instead of `TAIL`.
        Since MC 1.21.5, there is an early return used to check the signature of SignedMessage, making it not work in `offline mode`.
        """)
    @Inject(method = "sendChatMessage", at = @At(value = "RETURN"))
    void storeChatHistoryWhenSentMessage(SignedMessage signedMessage, MessageType.Parameters parameters, CallbackInfo ci) {
        ChatHistoryInitializer.processChatHistory(signedMessage, parameters);
    }

}
