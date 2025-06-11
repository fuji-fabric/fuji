package io.github.sakurawald.module.mixin.disabler.move_wrongly_disabler;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @ModifyExpressionValue(
        method = "onPlayerMove",
        at = @At(
            value = "INVOKE",
            #if MC_VER < MC_1_21_5
            target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"
            #elif MC_VER >= MC_1_21_5
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;isCreative()Z"
            #endif
        )
    )
    public boolean disablePlayerMovedWrongly(boolean original) {
        return true;
    }

    #if MC_VER < MC_1_21_6
    @ModifyConstant(method = "onVehicleMove", constant = @Constant(doubleValue = 0.0625, ordinal = 1))
    #elif MC_VER >= MC_1_21_6
    @ModifyConstant(method = "onVehicleMove", constant = @Constant(doubleValue = 0.0625, ordinal = 0))
    #endif
    public double disableVehicleMovedWrongly(double original) {
        return Double.MAX_VALUE;
     }

}
