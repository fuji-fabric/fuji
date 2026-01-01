package mod.fuji.module.mixin.afk.effect;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.module.initializer.afk.effect.AfkEffectInitializer;
import mod.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = EventConsumer.HIGHEST)
public class EntityMixin {

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void processMoveableEffect(MoverType movementType, Vec3 vec3d, CallbackInfo ci) {
        PlayerHelper.Kind.ifServerPlayerEntity(this,player -> {
            if (!AfkEffectInitializer.config.model().moveable
                && AfkService.isInAfkState(player)) {

                /* Store the originalX before the call to move(). */
                double originalX = player.getX();
                double originalY = player.getY();
                double originalZ = player.getZ();

                /* Send a packet to re-sync the position between the client and server. */
                player.teleportTo(originalX, originalY, originalZ);

                /* Cancel this move() call in the server. */
                ci.cancel();
            }
        });
    }
}
