package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerDeathEvent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(ServerPlayerEntity.class)
public class PlayerDeathEventMixin {

    @EventProducer(PlayerDeathEvent.class)
    @Inject(method = "onDeath", at = @At("HEAD"))
    public void produceOnPlayerDeathEvent(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerDeathEvent event = new PlayerDeathEvent(player);
        EventManager.dispatchEvent(PlayerDeathEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
