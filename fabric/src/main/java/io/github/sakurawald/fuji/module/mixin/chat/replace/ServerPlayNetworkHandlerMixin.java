package io.github.sakurawald.fuji.module.mixin.chat.replace;

import io.github.sakurawald.fuji.module.initializer.chat.replace.ChatReplaceInitializer;
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

    /* If you hack the onChatMessage method, and modify the chat string in chat packet, then you will break the chat signature in online-mode server.
    *  The only possible way is to keep the chat string and signature, but modify the chat text. (In online-mode server, the client will complain that the chat text is modified by the server, but it will still allow the player to see and play in the server.)
    * */
    @ModifyArgs(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    public void modifyChatMessageSentByPlayers(Args args) {
        /* Get signed message. */
        SignedMessage signedMessage = args.get(0);

        /* Replace the text. */
        Text oldValue = signedMessage.getContent();
        Text newValue = ChatReplaceInitializer.replaceChatText(player, oldValue);
        args.set(0, signedMessage.withUnsignedContent(newValue));
    }
}
