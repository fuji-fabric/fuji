package io.github.sakurawald.module.mixin.command_attachment;

import io.github.sakurawald.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.core.auxiliary.minecraft.NbtHelper;
import io.github.sakurawald.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.module.initializer.command_attachment.CommandAttachmentInitializer;
import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.InteractType;
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
        String uuid = UuidHelper.getAttachedUuid(NbtHelper.getNbt(itemStack));
        if (uuid == null) return;

        CommandAttachmentInitializer.triggerAttachmentModel(uuid, player, List.of(InteractType.RIGHT, InteractType.BOTH));
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
            if (!CommandAttachmentInitializer.existsAttachmentModel(uuid)) return;
            CommandAttachmentInitializer.triggerAttachmentModel(uuid, player, List.of(InteractType.LEFT, InteractType.BOTH));
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    void onPlayerRightClickBlock(ServerPlayerEntity serverPlayerEntity, @NotNull World world, ItemStack itemStack, Hand hand, @NotNull BlockHitResult blockHitResult, @NotNull CallbackInfoReturnable<ActionResult> cir) {

        if (hand == Hand.MAIN_HAND) {
            String uuid = UuidHelper.getAttachedUuid(world, blockHitResult.getBlockPos());
            if (!CommandAttachmentInitializer.existsAttachmentModel(uuid)) return;
            CommandAttachmentInitializer.triggerAttachmentModel(uuid, player, List.of(InteractType.RIGHT, InteractType.BOTH));
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

}
