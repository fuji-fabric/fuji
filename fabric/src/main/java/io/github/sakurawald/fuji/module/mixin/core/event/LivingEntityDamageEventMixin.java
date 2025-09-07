package io.github.sakurawald.fuji.module.mixin.core.event;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.entity.LivingEntityDamageEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@PhasedMixinTemplate
@Mixin(LivingEntity.class)
public class LivingEntityDamageEventMixin {

    @EventProducer(LivingEntityDamageEvent.class)
    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), argsOnly = true)
    float produceLivingEntityDamageEvent(float damage, @Local(argsOnly = true) DamageSource damageSource) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        LivingEntityDamageEvent event = new LivingEntityDamageEvent(entity, damageSource, damage);
        EventManager.dispatchEvent(LivingEntityDamageEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getDamage();
    }

}
