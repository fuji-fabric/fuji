package io.github.sakurawald.module.mixin.chat.rewrite;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.module.initializer.chat.rewrite.ChatRewriteInitializer;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 + 1000)
public abstract class ServerPlayNetworkHandlerMixin {

    @ModifyVariable(method = "onChatMessage", at = @At(value = "HEAD"), argsOnly = true)
    public ChatMessageC2SPacket modifyChatMessageSentByPlayers(ChatMessageC2SPacket original) {
        String oldChatMessage = original.chatMessage();
        String newChatMessage = ChatRewriteInitializer.rewriteChatString(oldChatMessage);
        LogUtil.debug("rewrite chat message: old = {}, new = {}", oldChatMessage, newChatMessage);

        return new ChatMessageC2SPacket(newChatMessage, original.timestamp(), original.comp_947(), original.comp_948(), original.acknowledgment());
    }
}
