package io.github.sakurawald.module.mixin.multiplier;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    @Unique
    @NotNull
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Unique
    float transform(@NotNull ServerPlayerEntity player, String type, String key, float f) {
        Optional<Float> meta = PermissionHelper.getMeta(player.getUuid(), "fuji.multiplier.%s.%s".formatted(type, key), Float::valueOf);
        return meta.map(factor -> f * factor).orElse(f);
    }

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), argsOnly = true)
    public float transformDamage(float damage, @Local(argsOnly = true) @NotNull DamageSource damageSource) {
        damage = transform(player, "damage", "all", damage);
        damage = transform(player, "damage", damageSource.getTypeRegistryEntry().getIdAsString(), damage);
        return damage;
    }

    @ModifyVariable(method = "addExperience", at = @At(value = "HEAD"), argsOnly = true)
    public int transformExperience(int exp) {
        exp = (int) transform(player, "experience", "all", exp);
        return exp;
    }

}
