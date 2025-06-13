package io.github.sakurawald.module.mixin.chat.spy;

import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.module.initializer.chat.spy.ChatSpyInitializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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
    public void spy(SignedMessage signedMessage, MessageType.Parameters parameters, CallbackInfo ci) {
        /* Extract the message type string. */
        #if MC_VER <= MC_1_20_4
        MessageType messageTypeObj = parameters.type();
        String messageTypeString = RegistryHelper.findRegistryKeyByRegistryValueInASpecifiedRegistry(RegistryKeys.MESSAGE_TYPE, messageTypeObj);
        #elif MC_VER > MC_1_20_4
        String messageTypeString = parameters.type().getIdAsString();
        #endif

        /* Process it. */
        ChatSpyInitializer.processChatSpy(messageTypeString, getPlayer(), signedMessage, parameters);
    }

}
