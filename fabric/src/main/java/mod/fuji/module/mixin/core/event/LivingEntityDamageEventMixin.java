package mod.fuji.module.mixin.core.event;

import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.entity.LivingEntityDamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@PhasedMixinTemplate
@Mixin(LivingEntity.class)
public class LivingEntityDamageEventMixin {

    @EventProducer(LivingEntityDamageEvent.class)
    @ModifyVariable(method = "hurtServer", at = @At(value = "HEAD"), argsOnly = true)
    float produceLivingEntityDamageEvent(float damage, @Local(argsOnly = true) DamageSource damageSource) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        LivingEntityDamageEvent event = new LivingEntityDamageEvent(entity, damageSource, damage);
        EventManager.dispatchEvent(LivingEntityDamageEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getDamage();
    }

}
