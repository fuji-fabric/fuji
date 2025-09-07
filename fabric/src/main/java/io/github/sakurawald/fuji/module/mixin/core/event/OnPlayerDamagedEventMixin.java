package io.github.sakurawald.fuji.module.mixin.core.event;

import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerDamageEvent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@PhasedMixinTemplate
@Mixin(value = ServerPlayerEntity.class)
public class OnPlayerDamagedEventMixin {

    @EventProducer(PlayerDamageEvent.class)
    @Inject(method = "damage", at = @At("RETURN"))
    public void produceOnPlayerDamagedEvent(
        #if MC_VER <= MC_1_21
        #elif MC_VER > MC_1_21
        ServerWorld serverWorld,
        #endif
        DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerDamageEvent event = new PlayerDamageEvent(player);
        EventManager.dispatchEvent(PlayerDamageEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
