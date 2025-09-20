package io.github.sakurawald.fuji.module.mixin.core.event;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.config.mapper.wrapper.GameProfileWrapper;
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
    #if MC_VER < MC_1_21_9
    @ModifyExpressionValue(
        method = "canBypassPlayerLimit",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/OperatorList;canBypassPlayerLimit(Lcom/mojang/authlib/GameProfile;)Z")
    )
    boolean produceBypassPlayerLimitEvent(boolean original, GameProfile vanillaType)
    #elif MC_VER >= MC_1_21_9
    @ModifyExpressionValue(
        method = "canBypassPlayerLimit",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/OperatorList;canBypassPlayerLimit(Lnet/minecraft/server/PlayerConfigEntry;)Z")
    )
    boolean produceBypassPlayerLimitEvent(boolean original, net.minecraft.server.PlayerConfigEntry vanillaType)
    #endif
    {
        return GameProfileWrapper
            .fromVanillaType(vanillaType)
            .toGameProfile()
            .map(gameProfile -> {
                BypassPlayerLimitEvent event = new BypassPlayerLimitEvent(gameProfile, original);
                EventManager.dispatchEvent(BypassPlayerLimitEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
                return event.isCanBypass();
            })
            .orElse(original);

    }
}
