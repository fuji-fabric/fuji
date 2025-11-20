package mod.fuji.module.mixin.afk.effect;

import mod.fuji.module.initializer.afk.effect.AfkEffectInitializer;
import mod.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    void handleTargetableEffect(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof ServerPlayer player) {
            if (!AfkEffectInitializer.config.model().targetable
                && AfkService.isAfk(player)) {
                cir.setReturnValue(false);
            }
        }
    }

}
