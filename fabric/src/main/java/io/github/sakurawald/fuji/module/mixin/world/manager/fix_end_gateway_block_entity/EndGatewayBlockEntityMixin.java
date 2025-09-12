package io.github.sakurawald.fuji.module.mixin.world.manager.fix_end_gateway_block_entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndGatewayBlockEntity.class)
public abstract class EndGatewayBlockEntityMixin {

    // NOTE: In vanilla Minecraft, the EndGatewayBlockEntity only checks the serverWorld.getRegistryKey() == World.END condition.
    // To make it working in extra dimensions, we make the expression to be always TRUE. (Actually, the EndGatewayBlockEntity will only appear in the_end dimension type)

    #if MC_VER <= MC_1_20_6
    @WrapOperation(method = "tryTeleportingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    private static RegistryKey<World> letExitPortalsInExtraDimensionsWork(World instance, Operation<RegistryKey<World>> original) {
        return World.END;
    }
    #elif MC_VER > MC_1_20_6
    @WrapOperation(method = "getOrCreateExitPortalPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    private RegistryKey<World> letExitPortalsInExtraDimensionsWork(net.minecraft.server.world.ServerWorld instance, Operation<RegistryKey<World>> original) {
        return World.END;
    }
    #endif
}
