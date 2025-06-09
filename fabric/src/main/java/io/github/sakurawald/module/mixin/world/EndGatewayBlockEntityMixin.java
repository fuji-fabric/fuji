package io.github.sakurawald.module.mixin.world;

import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndGatewayBlockEntity.class)
public abstract class EndGatewayBlockEntityMixin {

    // note: for a resource end, we force make World.END == World.END a true condition, so that the end gateway in resource end can work.
    @Redirect(method = "getOrCreateExitPortalPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    private RegistryKey<World> letExitPortalsInExtraDimensionsWork(ServerWorld instance) {
        return World.END;
    }
}
