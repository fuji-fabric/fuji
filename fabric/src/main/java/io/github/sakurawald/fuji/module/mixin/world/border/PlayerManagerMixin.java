package io.github.sakurawald.fuji.module.mixin.world.border;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.world.border.WorldBorderInitializer;
import io.github.sakurawald.fuji.module.initializer.world.border.structure.BorderDescriptor;
import io.github.sakurawald.fuji.module.initializer.world.border.structure.PerDimensionWorldBorderListener;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @ModifyArg(method = "sendWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/WorldBorderInitializeS2CPacket;<init>(Lnet/minecraft/world/border/WorldBorder;)V", ordinal = 0))
    WorldBorder modifyWorldBorderInitializePacket(WorldBorder original, @Local(argsOnly = true) ServerWorld serverWorld) {
        return WorldBorderInitializer
            .getEffectiveBorderDescriptor(RegistryHelper.getIdAsString(serverWorld))
            .map(BorderDescriptor::asVanillaWorldBorder)
            .orElse(original);
    }

    @Inject(method = "sendWorldInfo", at = @At("TAIL"))
    void ensureClientSideWorldBorderIsSyncedAfterTeleport(ServerPlayerEntity player, ServerWorld serverWorld, CallbackInfo ci) {
        WorldBorderInitializer.sendWorldBorderSyncPacketsToPlayer(player, serverWorld);
    }

    @ModifyArg(method = "setMainWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;addListener(Lnet/minecraft/world/border/WorldBorderListener;)V", ordinal = 0))
    WorldBorderListener registerPerDimensionWorldBorderListener(WorldBorderListener original, @Local(argsOnly = true) ServerWorld serverWorld) {
        return new PerDimensionWorldBorderListener();
    }

}
