package io.github.sakurawald.fuji.module.mixin.launcher;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.fuji.module.initializer.launcher.LauncherInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), argsOnly = true)
    float safelyLanding(float damage, @Local(argsOnly = true) DamageSource damageSource) {
        Entity entity = ((LivingEntity) (Object) this);

        boolean immuneToThisDamageType = damageSource
            .getTypeRegistryEntry()
            .getKey()
            .map(key -> key.equals(DamageTypes.FALL))
            .orElse(false);

        if (immuneToThisDamageType && LauncherInitializer.LAUNCHED_ENTITIES.contains(entity)) {
            LauncherInitializer.LAUNCHED_ENTITIES.remove(entity);
            return 0;
        }

        return damage;
    }
}
