package io.github.sakurawald.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerInteractEntityPreEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@PhasedMixinTemplate
@Mixin(Entity.class)
public class PlayerInteractEntityPreEventMixin {

    @EventProducer(PlayerInteractEntityPreEvent.class)
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    void producePlayerInteractEntityPreEvent(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ServerHelper.withServerPlayerEntity(player, serverPlayer -> {
            Entity entity = (Entity) (Object) this;
            PlayerInteractEntityPreEvent event = new PlayerInteractEntityPreEvent(serverPlayer, entity, hand, cir);
            EventManager.dispatchEvent(PlayerInteractEntityPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        });
    }

}
