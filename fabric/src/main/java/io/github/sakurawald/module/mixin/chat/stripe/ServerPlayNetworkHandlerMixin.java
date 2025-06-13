package io.github.sakurawald.module.mixin.chat.stripe;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.service.style_striper.StyleStriper;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// the pipeline of onChatMessage() should be: chat.stripe -> chat.mention -> chat.rewrite
@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 + 750)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private static final String STYLE_TYPE_CHAT = "chat";

    @Shadow
    public ServerPlayerEntity player;

    @ModifyVariable(method = "onChatMessage", at = @At(value = "HEAD"), argsOnly = true)
    public ChatMessageC2SPacket modifyChatMessageSentByPlayers(ChatMessageC2SPacket original) {
        String oldChatMessage = original.chatMessage();
        String newChatMessage = StyleStriper.stripe(player, STYLE_TYPE_CHAT, oldChatMessage);
        LogUtil.debug("Stripe chat message: old = {}, new = {}", oldChatMessage, newChatMessage);

        return new ChatMessageC2SPacket(newChatMessage, original.timestamp(), original.comp_947(), original.comp_948(), original.acknowledgment());
    }
}
