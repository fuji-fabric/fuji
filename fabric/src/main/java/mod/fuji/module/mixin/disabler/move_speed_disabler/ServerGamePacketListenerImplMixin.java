package mod.fuji.module.mixin.disabler.move_speed_disabler;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
#if MC_VER > MC_1_21
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
#endif
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    #if MC_VER <= MC_1_21
    @ModifyExpressionValue(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;isSingleplayerOwner()Z"))
    public boolean disablePlayerMoveTooQuickly(boolean original) {
        return true;
    }
    #elif MC_VER > MC_1_21
    @ModifyReturnValue(method = "shouldCheckPlayerMovement", at = @At("RETURN"))
    public boolean disablePlayerMoveTooQuickly(boolean original) {
        return false;
    }
    #endif


    @ModifyExpressionValue(
        method = "handleMoveVehicle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;isSingleplayerOwner()Z"
        )
    )
    public boolean disableVehicleMoveTooQuickly(boolean original) {
        return true;
    }
}
