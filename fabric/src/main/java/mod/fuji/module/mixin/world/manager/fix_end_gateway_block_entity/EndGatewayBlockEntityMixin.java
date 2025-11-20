package mod.fuji.module.mixin.world.manager.fix_end_gateway_block_entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TheEndGatewayBlockEntity.class)
public abstract class EndGatewayBlockEntityMixin {

    // NOTE: In vanilla Minecraft, the EndGatewayBlockEntity only checks the serverWorld.getRegistryKey() == World.END condition.
    // To make it working in extra dimensions, we make the expression to be always TRUE. (Actually, the EndGatewayBlockEntity will only appear in the_end dimension type)

    #if MC_VER <= MC_1_20_6
    @WrapOperation(method = "teleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"))
    private static ResourceKey<Level> letExitPortalsInExtraDimensionsWork(Level instance, Operation<ResourceKey<Level>> original) {
        return Level.END;
    }
    #elif MC_VER > MC_1_20_6
    @WrapOperation(method = "getPortalPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<Level> letExitPortalsInExtraDimensionsWork(net.minecraft.server.level.ServerLevel instance, Operation<ResourceKey<Level>> original) {
        return Level.END;
    }
    #endif
}
