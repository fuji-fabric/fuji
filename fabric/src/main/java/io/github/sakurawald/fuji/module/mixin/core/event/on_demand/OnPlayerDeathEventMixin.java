package io.github.sakurawald.fuji.module.mixin.core.event.on_demand;

import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.OnPlayerDeathEvent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(ServerPlayerEntity.class)
public class OnPlayerDeathEventMixin {

    @EventProducer(OnPlayerDeathEvent.class)
    @Inject(method = "onDeath", at = @At("HEAD"))
    public void produceOnPlayerDeathEvent(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        OnPlayerDeathEvent event = new OnPlayerDeathEvent(player);
        EventManager.dispatchEvent(OnPlayerDeathEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
