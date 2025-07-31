package io.github.sakurawald.fuji.module.mixin.nametag;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER < MC_1_21
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import org.spongepowered.asm.mixin.injection.At;
#endif

@Mixin(Entity.class)
public class EntityMixin {

    #if MC_VER < MC_1_21
    @ModifyExpressionValue(method = "canUsePortals", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasPassengers()Z"))
    boolean allowTeleportWithPassengers(boolean original) {
        // NOTE: In newer Minecraft versions the Entity#canUsePortals method will not check the hasPassenger() flag.
        Entity entity = (Entity) (Object) this;
        if (EntityHelper.isPlayerEntity(entity)) {
            return false;
        }

        return original;
    }
    #endif
}
