package mod.fuji.module.mixin.core.event;


import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerChatMessagePostEvent;
import mod.fuji.core.event.message.player.PlayerChatMessagePreEvent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@PhasedMixinTemplate
@Mixin(value = ServerGamePacketListenerImpl.class)
public class PlayerChatMessageEventMixin {

    @Shadow
    public ServerPlayer player;

    @EventProducer(PlayerChatMessagePreEvent.class)
    @ModifyArgs(method = "broadcastChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"))
    public void produceOnPlayerChatPreEvent(Args args) {
        PlayerChatMessage signedMessage = args.get(0);
        ChatType.Bound parameters = args.get(2);

        PlayerChatMessagePreEvent event = new PlayerChatMessagePreEvent(player, signedMessage, parameters);
        EventManager.dispatchEvent(PlayerChatMessagePreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);

        args.set(0, event.getSignedMessage());
        args.set(2, event.getParameters());
    }

    @EventProducer(PlayerChatMessagePostEvent.class)
    @Inject(method = "broadcastChatMessage", at = @At(value = "TAIL", target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"))
    public void produceOnPlayerChatPostEvent(PlayerChatMessage signedMessage, CallbackInfo ci) {
        PlayerChatMessagePostEvent event = new PlayerChatMessagePostEvent(player, signedMessage);
        EventManager.dispatchEvent(PlayerChatMessagePostEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
