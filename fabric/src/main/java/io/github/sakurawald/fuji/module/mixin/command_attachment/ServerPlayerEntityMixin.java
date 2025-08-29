package io.github.sakurawald.fuji.module.mixin.command_attachment;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "swingHand", at = @At("HEAD"))
    void onPlayerLeftClick(Hand hand, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (hand.equals(Hand.MAIN_HAND)) {
            ItemStack mainHandStack = player.getMainHandStack();
            String uuid = UuidHelper.getAttachedUuid(ItemStackHelper.CustomData.getCustomDataNbt(mainHandStack));

            CommandAttachmentService.tryTriggerAttachmentDataNode(uuid, player, List.of(InteractType.LEFT_CLICK, InteractType.ANY_CLICK), () -> {});
        }

    }

}
