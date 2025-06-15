package io.github.sakurawald.module.mixin.chat.mention;

import io.github.sakurawald.module.initializer.chat.mention.ChatMentionInitializer;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 + 750)
public abstract class ServerPlayNetworkHandlerMixin {

    @ModifyArgs(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    public void modifyChatMessageSentByPlayers(Args args) {
        /* Get signed message. */
        SignedMessage signedMessage = args.get(0);

        /* Replace the text. */
        Text oldValue = signedMessage.getContent();
        Text newValue = ChatMentionInitializer.replaceMentionText(oldValue);
        args.set(0, signedMessage.withUnsignedContent(newValue));
    }

}
