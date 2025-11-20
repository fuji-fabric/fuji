package mod.fuji.module.mixin.command_attachment;

import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.UuidHelper;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import mod.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "swing", at = @At("HEAD"))
    void onPlayerLeftClick(InteractionHand hand, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (hand.equals(InteractionHand.MAIN_HAND)) {
            ItemStack mainHandStack = player.getMainHandItem();
            UuidHelper
                .getAttachedUuid(ItemStackHelper.CustomData.getCustomDataNbt(mainHandStack))
                .ifPresent($uuid -> {
                    CommandAttachmentService.tryTriggerAttachmentDataNode($uuid, player, List.of(InteractType.LEFT_CLICK, InteractType.ANY_CLICK), () -> {});
                });
        }

    }

}
