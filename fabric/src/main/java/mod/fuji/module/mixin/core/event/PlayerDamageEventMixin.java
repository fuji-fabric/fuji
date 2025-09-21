package mod.fuji.module.mixin.core.event;

import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerDamageEvent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@PhasedMixinTemplate
@Mixin(value = ServerPlayerEntity.class)
public class PlayerDamageEventMixin {

    @EventProducer(PlayerDamageEvent.class)
    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), argsOnly = true)
    float producePlayerDamageEvent(float damage, @Local(argsOnly = true) DamageSource damageSource) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerDamageEvent event = new PlayerDamageEvent(player, damageSource, damage);
        EventManager.dispatchEvent(PlayerDamageEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getDamage();
    }
}
