package io.github.sakurawald.fuji.module.mixin.core.event;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.server.BypassPlayerLimitEvent;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@PhasedMixinTemplate
@Mixin(DedicatedPlayerManager.class)
public class BypassPlayerLimitEventMixin {

    @EventProducer(BypassPlayerLimitEvent.class)
    @ModifyExpressionValue(
        method = "canBypassPlayerLimit",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/OperatorList;canBypassPlayerLimit(Lcom/mojang/authlib/GameProfile;)Z")
    )
    boolean produceBypassPlayerLimitEvent(boolean original, GameProfile profile) {
        BypassPlayerLimitEvent event = new BypassPlayerLimitEvent(profile, original);
        EventManager.dispatchEvent(BypassPlayerLimitEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.isCanBypass();
    }
}
