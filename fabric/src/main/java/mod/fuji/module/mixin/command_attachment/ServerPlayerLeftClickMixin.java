package mod.fuji.module.mixin.command_attachment;

import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.UuidHelper;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import mod.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

#if MC_VER < MC_26_3
@Mixin(ServerPlayer.class)
#elif MC_VER >= MC_26_3
@Mixin(LivingEntity.class)
#endif
public abstract class ServerPlayerLeftClickMixin {

    @Inject(method = "swing", at = @At("HEAD"))
    #if MC_VER < MC_26_3
    void onPlayerLeftClick(InteractionHand hand, CallbackInfo ci)
    #elif MC_VER >= MC_26_3
    void onPlayerLeftClick(InteractionHand hand, SwingAnimation animation, boolean sendToSwingingEntity, CallbackInfoReturnable<Boolean> cir)
    #endif
    {
        PlayerHelper.Kind.ifServerPlayerEntity(this, player -> {
            if (hand.equals(InteractionHand.MAIN_HAND)) {
                ItemStack mainHandStack = player.getMainHandItem();
                UuidHelper
                    .getAttachedUuid(ItemStackHelper.CustomData.getCustomDataNbt(mainHandStack))
                    .ifPresent($uuid -> {
                        CommandAttachmentService.tryTriggerAttachmentDataNode($uuid, player, List.of(InteractType.LEFT_CLICK, InteractType.ANY_CLICK), () -> {});
                    });
            }
        });
    }

}
