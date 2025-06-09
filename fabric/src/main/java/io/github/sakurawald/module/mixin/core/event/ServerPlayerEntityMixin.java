package io.github.sakurawald.module.mixin.core.event;

import io.github.sakurawald.core.event.impl.PlayerEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    public void abortTicketIfGetDamaged(ServerWorld serverWorld, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        // If damage was actually applied...
        if (cir.getReturnValue()) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            PlayerEvents.ON_DAMAGED.invoker().fire(player, damageSource, f);
        }
    }
}
