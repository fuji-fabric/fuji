package mod.fuji.module.mixin.afk.effect;

import mod.fuji.module.initializer.afk.effect.AfkEffectInitializer;
import mod.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    void handleTargetableEffect(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof ServerPlayerEntity player) {
            if (!AfkEffectInitializer.config.model().targetable
                && AfkService.isAfk(player)) {
                cir.setReturnValue(false);
            }
        }
    }

}
