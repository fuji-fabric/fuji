package io.github.sakurawald.fuji.module.mixin.core.event;


import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.player.OnPlayerChatMessageEvent;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@PhasedMixinTemplate
@Mixin(value = ServerPlayNetworkHandler.class)
public class OnPlayerChatMessageEventMixin {

    @Shadow
    public ServerPlayerEntity player;

    @EventProducer(OnPlayerChatMessageEvent.class)
    @ModifyArgs(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    public void produceOnPlayerChatEvent(Args args) {
        SignedMessage signedMessage = args.get(0);
        MessageType.Parameters parameters = args.get(2);

        OnPlayerChatMessageEvent event = new OnPlayerChatMessageEvent(player, signedMessage, parameters);
        EventManager.dispatchEvent(OnPlayerChatMessageEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);

        args.set(0, event.getSignedMessage());
        args.set(2, event.getParameters());
    }

}
