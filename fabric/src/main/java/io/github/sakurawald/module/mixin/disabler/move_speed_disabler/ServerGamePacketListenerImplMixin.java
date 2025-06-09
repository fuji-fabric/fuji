package io.github.sakurawald.module.mixin.disabler.move_speed_disabler;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayerEntity player;

    @ModifyReturnValue(method = "shouldCheckMovement", at = @At("RETURN"))
    public boolean disablePlayerMoveTooQuickly(boolean original) {
        return false;
    }

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
