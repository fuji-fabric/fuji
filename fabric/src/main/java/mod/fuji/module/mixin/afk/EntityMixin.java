package mod.fuji.module.mixin.afk;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @TestCase(action = "Try to move a player in afk state.", targets = "The `moveable` option should work.")
    @Inject(method = "move", at = @At("HEAD"))
    public void countActionOnPlayerMove(MoverType movementType, Vec3 vec3d, CallbackInfo ci) {
        PlayerHelper.Kind.ifServerPlayerEntity(this, serverPlayer -> {
            if (AfkService.isPlayerMovedBySelf(movementType, vec3d)) {
                AfkService.receiveAction(serverPlayer);
            }
        });
    }

}
