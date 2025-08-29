package io.github.sakurawald.fuji.module.mixin.command_attachment;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import java.util.List;
import net.minecraft.item.ItemStack;
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
    void onPlayerSwapHand(PlayerActionC2SPacket playerActionC2SPacket, CallbackInfo ci) {
        if (playerActionC2SPacket.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            ItemStack itemStack = player.getMainHandStack();
            String uuid = UuidHelper.getAttachedUuid(ItemStackHelper.CustomData.getCustomDataNbt(itemStack));

            CommandAttachmentService.tryTriggerAttachmentDataNode(uuid, player, List.of(InteractType.SWAP_HAND), ci::cancel);
        }
    }

}
