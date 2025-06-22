package io.github.sakurawald.module.mixin.command_menu;

import io.github.sakurawald.module.initializer.command_menu.CommandMenuInitializer;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    void onShiftAndSwapHandsEvent(PlayerActionC2SPacket playerActionC2SPacket, CallbackInfo ci) {
        if (!CommandMenuInitializer.config.model().onSneakingAndSwapHandsEvent.enable) return;

        if (playerActionC2SPacket.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND
            && player.isSneaking()) {
            CommandMenuInitializer.executeOnSneakingAndSwapHandsCommands(player);
            ci.cancel();
        }
    }

}
