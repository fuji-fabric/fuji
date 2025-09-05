package io.github.sakurawald.fuji.module.mixin.back;

import io.github.sakurawald.fuji.module.initializer.back.BackInitializer;
import java.util.Set;
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

@Mixin(value = ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Unique
    @NotNull
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    #if MC_VER <= MC_1_21
    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At("HEAD"))
    public void saveLocationOnTeleport(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci)
    {
        BackInitializer.trySaveCurrentLocationOnTeleport(player);
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", at = @At("HEAD"))
    public void saveLocationOnTeleport(ServerWorld serverWorld, double d, double e, double f, Set<PositionFlag> set, float g, float h, CallbackInfoReturnable<Boolean> cir) {
        BackInitializer.trySaveCurrentLocationOnTeleport(player);
    }
    #elif MC_VER > MC_1_21
    @Inject(method = "teleport", at = @At("HEAD"))
    public void saveLocationOnTeleport(ServerWorld serverWorld, double d, double e, double f, Set<PositionFlag> set, float g, float h, boolean bl, CallbackInfoReturnable<Boolean> cir)
    {
        BackInitializer.trySaveCurrentLocationOnTeleport(player);
    }
    #endif




}
