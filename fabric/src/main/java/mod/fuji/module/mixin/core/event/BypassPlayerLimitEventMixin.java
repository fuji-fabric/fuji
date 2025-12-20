package mod.fuji.module.mixin.core.event;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.config.mapper.representation.GameProfileIR;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.server.BypassPlayerLimitEvent;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@PhasedMixinTemplate
@Mixin(DedicatedPlayerList.class)
public class BypassPlayerLimitEventMixin {

    @EventProducer(BypassPlayerLimitEvent.class)
    #if MC_VER < MC_1_21_9
    @ModifyExpressionValue(
        method = "canBypassPlayerLimit",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/ServerOpList;canBypassPlayerLimit(Lcom/mojang/authlib/GameProfile;)Z")
    )
    boolean produceBypassPlayerLimitEvent(boolean original, com.mojang.authlib.GameProfile nativeValue)
    #elif MC_VER >= MC_1_21_9
    @ModifyExpressionValue(
        method = "canBypassPlayerLimit",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/ServerOpList;canBypassPlayerLimit(Lnet/minecraft/server/players/NameAndId;)Z")
    )
    boolean produceBypassPlayerLimitEvent(boolean original, net.minecraft.server.players.NameAndId nativeValue)
    #endif
    {
        return GameProfileIR
            .from(nativeValue)
            .toGameProfile()
            .map(gameProfile -> {
                BypassPlayerLimitEvent event = new BypassPlayerLimitEvent(gameProfile, original);
                EventManager.dispatchEvent(BypassPlayerLimitEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
                return event.isCanBypass();
            })
            .orElse(original);

    }
}
