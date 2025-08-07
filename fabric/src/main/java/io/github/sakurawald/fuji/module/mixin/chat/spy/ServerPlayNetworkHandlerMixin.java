package io.github.sakurawald.fuji.module.mixin.chat.spy;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.chat.spy.ChatSpyInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "sendChatMessage", at = @At(value = "HEAD"))
    public void onChatMessageSentToAllOnlinePlayers(SignedMessage signedMessage, MessageType.Parameters parameters, CallbackInfo ci) {
        /* Extract the message type string. */
        String messageTypeString = RegistryHelper.getIdAsString(parameters);

        /* Process it. */
        Text contentText = parameters.applyChatDecoration(signedMessage.getContent());
        ChatSpyInitializer.processChatSpy(messageTypeString, getPlayer(), contentText);
    }

}
