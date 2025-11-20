package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerWorldChangedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(ServerPlayer.class)
public class PlayerWorldChangedEventMixin {

    @EventProducer(PlayerWorldChangedEvent.class)
    @Inject(method = "triggerDimensionChangeTriggers(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("TAIL"))
    void producePlayerWorldChangedEvent(ServerLevel srcWorld, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel destWorld = PlayerHelper.getServerWorld(player);
        PlayerWorldChangedEvent event = new PlayerWorldChangedEvent(player, srcWorld, destWorld);
        EventManager.dispatchEvent(PlayerWorldChangedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
