package io.github.sakurawald.fuji.module.mixin.core.event;

import io.github.sakurawald.fuji.core.event.message.impl.PlayerEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
#if MC_VER > MC_1_21
import net.minecraft.server.world.ServerWorld;
#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    public void abortTicketIfGetDamaged(
        #if MC_VER <= MC_1_21
        #elif MC_VER > MC_1_21
            ServerWorld serverWorld,
        #endif
        DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        // If damage was actually applied...
        if (cir.getReturnValue()) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            PlayerEvents.ON_DAMAGED.invoker().fire(player, damageSource, f);
        }
    }
}
