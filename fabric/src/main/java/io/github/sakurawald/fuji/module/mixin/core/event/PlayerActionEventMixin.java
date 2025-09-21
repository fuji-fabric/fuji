package io.github.sakurawald.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerActionEvent;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(ServerPlayNetworkHandler.class)
public class PlayerActionEventMixin {

    @Shadow
    public ServerPlayerEntity player;

    @EventProducer(PlayerActionEvent.class)
    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    void producePlayerActionEvent(PlayerActionC2SPacket packet, CallbackInfo ci) {
        PlayerActionEvent event = new PlayerActionEvent(player, packet, ci);
        EventManager.dispatchEvent(PlayerActionEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
