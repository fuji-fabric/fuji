package mod.fuji.module.mixin.disabler.move_speed_disabler;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
#if MC_VER > MC_1_21
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
#endif
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayerEntity player;

    #if MC_VER <= MC_1_21
    @ModifyExpressionValue(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;isHost()Z"))
    public boolean disablePlayerMoveTooQuickly(boolean original) {
        return true;
    }
    #elif MC_VER > MC_1_21
    @ModifyReturnValue(method = "shouldCheckMovement", at = @At("RETURN"))
    public boolean disablePlayerMoveTooQuickly(boolean original) {
        return false;
    }
    #endif


    @ModifyExpressionValue(
        method = "onVehicleMove",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;isHost()Z"
        )
    )
    public boolean disableVehicleMoveTooQuickly(boolean original) {
        return true;
    }
}
