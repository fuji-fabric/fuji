package io.github.sakurawald.fuji.module.mixin.command_attachment;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "interactItem", at = @At("HEAD"))
    void onPlayerRightClick(ServerPlayerEntity serverPlayerEntity, World world, @NotNull ItemStack itemStack, Hand hand, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        String uuid = UuidHelper.getAttachedUuid(ItemStackHelper.CustomData.getCustomDataNbt(itemStack));

        CommandAttachmentService.tryTriggerAttachmentModel(uuid, player, List.of(InteractType.RIGHT, InteractType.BOTH));
    }

    #if MC_VER <= MC_1_20_4
    @Inject(method = "method_41250", at = @At("HEAD"))
    #elif MC_VER > MC_1_20_4
    @Inject(method = "onBlockBreakingAction", at = @At("HEAD"))
    #endif
    void onPlayerLeftClickBlock(BlockPos blockPos, boolean bl, int i, String string, CallbackInfo ci)
     {
        if (string.equals("actual start of destroying")) {
            String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);

            CommandAttachmentService.tryTriggerAttachmentModel(uuid, player, List.of(InteractType.LEFT, InteractType.BOTH));
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    void onPlayerRightClickBlock(ServerPlayerEntity serverPlayerEntity, @NotNull World world, ItemStack itemStack, Hand hand, @NotNull BlockHitResult blockHitResult, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        if (hand == Hand.MAIN_HAND) {
            String uuid = UuidHelper.getAttachedUuid(world, blockHitResult.getBlockPos());
            CommandAttachmentService.tryTriggerAttachmentModel(uuid, player, List.of(InteractType.RIGHT, InteractType.BOTH), () -> {
                // Cancel the action if the target block contains attached commands.
                cir.setReturnValue(ActionResult.FAIL);
            });
        }
    }

}
