package io.github.sakurawald.module.mixin.back;

import io.github.sakurawald.module.initializer.back.BackInitializer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Unique
    @NotNull
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void saveCurPos(DamageSource damageSource, CallbackInfo ci) {
        if (BackInitializer.config.model().enable_back_on_death) {
            BackInitializer.trySaveCurrentLocation(player);
        }
    }

    @Inject(method = "teleport", at = @At("HEAD"))
    public void saveCurPos(ServerWorld serverWorld, double d, double e, double f, Set<PositionFlag> set, float g, float h, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (BackInitializer.config.model().enable_back_on_teleport) {
            BackInitializer.trySaveCurrentLocation(player);
        }
    }

}
