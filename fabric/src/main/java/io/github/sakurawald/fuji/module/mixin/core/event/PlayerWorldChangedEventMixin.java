package io.github.sakurawald.fuji.module.mixin.core.event;

import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerWorldChangedEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(ServerPlayerEntity.class)
public class PlayerWorldChangedEventMixin {

    @EventProducer(PlayerWorldChangedEvent.class)
    @Inject(method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
    void producePlayerWorldChangedEvent(ServerWorld srcWorld, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ServerWorld destWorld = player.getWorld();
        PlayerWorldChangedEvent event = new PlayerWorldChangedEvent(player, srcWorld, destWorld);
        EventManager.dispatchEvent(PlayerWorldChangedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
