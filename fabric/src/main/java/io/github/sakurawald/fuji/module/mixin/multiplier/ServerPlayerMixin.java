package io.github.sakurawald.fuji.module.mixin.multiplier;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.multiplier.MultiplierInitializer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    @Unique
    @NotNull
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), argsOnly = true)
    public float transformDamage(float damage, @Local(argsOnly = true) @NotNull DamageSource damageSource) {
        damage = MultiplierInitializer.transform(player, "damage", "all", damage);
        damage = MultiplierInitializer.transform(player, "damage", RegistryHelper.getIdAsString(damageSource.getTypeRegistryEntry()), damage);
        return damage;
    }

    @ModifyVariable(method = "addExperience", at = @At(value = "HEAD"), argsOnly = true)
    public int transformExperience(int exp) {
        exp = (int) MultiplierInitializer.transform(player, "experience", "all", exp);
        return exp;
    }

}
