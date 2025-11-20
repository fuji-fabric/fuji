package mod.fuji.module.mixin.command_attachment;

import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.UuidHelper;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import mod.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "debugLogging", at = @At("HEAD"))
    void onPlayerLeftClickBlock(BlockPos blockPos, boolean bl, int i, String string, CallbackInfo ci) {
        if (string.equals("actual start of destroying")) {
            String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);
            CommandAttachmentService.tryTriggerAttachmentDataNode(uuid, player, List.of(InteractType.LEFT_CLICK, InteractType.ANY_CLICK), () -> {});
        }
    }

}
