package io.github.sakurawald.fuji.module.mixin.core.event;


import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerChatMessagePostEvent;
import io.github.sakurawald.fuji.core.event.message.player.PlayerChatMessagePreEvent;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@PhasedMixinTemplate
@Mixin(value = ServerPlayNetworkHandler.class)
public class PlayerChatMessageEventMixin {

    @Shadow
    public ServerPlayerEntity player;

    @EventProducer(PlayerChatMessagePreEvent.class)
    @ModifyArgs(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    public void produceOnPlayerChatPreEvent(Args args) {
        SignedMessage signedMessage = args.get(0);
        MessageType.Parameters parameters = args.get(2);

        PlayerChatMessagePreEvent event = new PlayerChatMessagePreEvent(player, signedMessage, parameters);
        EventManager.dispatchEvent(PlayerChatMessagePreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);

        args.set(0, event.getSignedMessage());
        args.set(2, event.getParameters());
    }

    @EventProducer(PlayerChatMessagePostEvent.class)
    @Inject(method = "handleDecoratedMessage", at = @At(value = "TAIL", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    public void produceOnPlayerChatPostEvent(SignedMessage signedMessage, CallbackInfo ci) {
        PlayerChatMessagePostEvent event = new PlayerChatMessagePostEvent(player, signedMessage);
        EventManager.dispatchEvent(PlayerChatMessagePostEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
