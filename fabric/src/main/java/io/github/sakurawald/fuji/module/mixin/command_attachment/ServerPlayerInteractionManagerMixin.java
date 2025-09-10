package io.github.sakurawald.fuji.module.mixin.command_attachment;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    #if MC_VER <= MC_1_20_4
    @Inject(method = "method_41250", at = @At("HEAD"))
    #elif MC_VER > MC_1_20_4
    @Inject(method = "onBlockBreakingAction", at = @At("HEAD"))
    #endif
    void onPlayerLeftClickBlock(BlockPos blockPos, boolean bl, int i, String string, CallbackInfo ci) {
        if (string.equals("actual start of destroying")) {
            String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);
            CommandAttachmentService.tryTriggerAttachmentDataNode(uuid, player, List.of(InteractType.LEFT_CLICK, InteractType.ANY_CLICK), () -> {});
        }
    }

}
