package io.github.sakurawald.module.mixin.anti_build;

import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    void $interact(@NotNull PlayerEntity player, Hand hand, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        Entity entity = (Entity) (Object) this;
        String id = RegistryHelper.ofString(entity);

        AntiBuildInitializer.checkAntiBuild(player, "interact_entity", AntiBuildInitializer.config.model().anti.interact_entity.id, id, cir, ActionResult.FAIL,() -> hand == Hand.MAIN_HAND);
    }

}
