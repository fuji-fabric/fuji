package io.github.sakurawald.fuji.module.mixin.afk.effect;

import io.github.sakurawald.fuji.module.initializer.afk.effect.AfkEffectInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    void handleTargetableEffect(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!AfkEffectInitializer.config.model().targetable
            && AfkService.isAfk(livingEntity)) {
            cir.setReturnValue(false);
        }
    }

}
