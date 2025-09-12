package io.github.sakurawald.fuji.module.mixin.afk;

import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @TestCase(action = "Try to move a player in afk state.", targets = "The `moveable` option should work.")
    @Inject(method = "move", at = @At("HEAD"))
    public void countActionOnPlayerMove(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        Object self = this;
        if (self instanceof ServerPlayerEntity player) {
            if (AfkService.isPlayerMovedBySelf(movementType, vec3d)) {
                AfkService.countAction(player);
            }
        }
    }

}
