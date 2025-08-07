package io.github.sakurawald.fuji.module.mixin.chat.style;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.chat.style.ChatStyleInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 + 250)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @ModifyArgs(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    public void modifyChatMessage(Args args) {
        /* Get signed message. */
        SignedMessage signedMessage = args.get(0);

        /* Make sender text. */
        Text senderText = ChatStyleInitializer.parseSenderText(player);
        args.set(2, MessageType.params(ChatStyleInitializer.MESSAGE_TYPE_KEY, ServerHelper.getServer().getRegistryManager(), senderText));

        /* Make content text. */
        String contentString = TextHelper.Operators.getString(signedMessage.getContent());
        Text contentText = ChatStyleInitializer.parseContentText(player, contentString);
        args.set(0, signedMessage.withUnsignedContent(contentText));
    }
}
