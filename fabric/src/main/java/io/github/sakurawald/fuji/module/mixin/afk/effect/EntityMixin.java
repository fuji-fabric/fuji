package io.github.sakurawald.fuji.module.mixin.afk.effect;

import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.module.initializer.afk.effect.AfkEffectInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = EventConsumer.HIGHEST)
public class EntityMixin {

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void processMoveableEffect(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        Object self = this;
        if (self instanceof ServerPlayerEntity player) {
            /* Handle moveable option. */
            if (!AfkEffectInitializer.config.model().moveable && AfkService.isAfk(player)) {
                /* Store the originalX before the call to move() */
                double originalX = player.getX();
                double originalY = player.getY();
                double originalZ = player.getZ();

                // Send packet to force set the position of the player in client-side. (If we didn't request a teleport for client, the position of player will de-sync between client and server)
                player.requestTeleport(originalX, originalY, originalZ);
                ci.cancel();
            }
        }

    }
}
