package io.github.sakurawald.fuji.module.mixin.chat.trigger;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.chat.trigger.ChatTriggerInitializer;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 + 1000)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "handleDecoratedMessage", at = @At(value = "TAIL"))
    public void listenChatString(SignedMessage signedMessage, CallbackInfo ci) {
        String chatString = TextHelper.Operators.getString(signedMessage.getContent());
        ChatTriggerInitializer.processChatTriggers(player.getCommandSource(), chatString);
    }
}
