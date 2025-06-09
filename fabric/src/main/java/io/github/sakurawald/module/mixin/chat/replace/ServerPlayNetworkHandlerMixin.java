package io.github.sakurawald.module.mixin.chat.replace;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.module.initializer.chat.replace.model.ChatReplaceInitializer;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 + 1000)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @ModifyArgs(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    public void modifyChatMessageSentByPlayers(Args args) {
        /* get args */
        SignedMessage signedMessage = args.get(0);

        /* make content text */
        Text oldValue = signedMessage.getContent();
        Text newValue = ChatReplaceInitializer.replaceChatText(player, oldValue);
        LogUtil.debug("replace chat text: old = {}, new = {}", oldValue, newValue);
        args.set(0, signedMessage.withUnsignedContent(newValue));
    }
}
