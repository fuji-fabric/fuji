package io.github.sakurawald.module.mixin.afk.effect;

import io.github.sakurawald.module.initializer.afk.AfkInitializer;
import io.github.sakurawald.module.initializer.afk.effect.AfkEffectInitializer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    void targetableEffect(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!AfkEffectInitializer.config.model().targetable && AfkInitializer.isAfk(livingEntity)) {
            cir.setReturnValue(false);
        }
    }

}
