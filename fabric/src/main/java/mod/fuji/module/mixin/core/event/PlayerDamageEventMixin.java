package mod.fuji.module.mixin.core.event;

import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerDamageEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@PhasedMixinTemplate
@Mixin(value = ServerPlayer.class)
public class PlayerDamageEventMixin {

    @EventProducer(PlayerDamageEvent.class)
    #if MC_VER <= MC_1_21
    @ModifyVariable(method = "hurt", at = @At(value = "HEAD"), argsOnly = true)
    #elif MC_VER > MC_1_21
    @ModifyVariable(method = "hurtServer", at = @At(value = "HEAD"), argsOnly = true)
    #endif
    float producePlayerDamageEvent(float damage, @Local(argsOnly = true) DamageSource damageSource) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        PlayerDamageEvent event = new PlayerDamageEvent(player, damageSource, damage);
        EventManager.dispatchEvent(PlayerDamageEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getDamage();
    }
}
