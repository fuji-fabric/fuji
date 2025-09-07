package io.github.sakurawald.fuji.module.mixin.core.event;


import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.OnPlayerJoinedEvent;
import io.github.sakurawald.fuji.core.event.message.player.OnPlayerLeftEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(PlayerManager.class)
public class PlayerManagerEventMixin {

    @EventProducer(OnPlayerJoinedEvent.class)
    #if MC_VER <= MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void produceOnPlayerJoinedEvent(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void produceOnPlayerJoinedEvent(ClientConnection clientConnection, @NotNull ServerPlayerEntity player, net.minecraft.server.network.ConnectedClientData connectedClientData, CallbackInfo ci)
    #endif {
        OnPlayerJoinedEvent event = new OnPlayerJoinedEvent(player);
        EventManager.dispatchEvent(OnPlayerJoinedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(OnPlayerLeftEvent.class)
    @Inject(method = "remove", at = @At("HEAD"))
    void produceOnPlayerLeaveEvent(ServerPlayerEntity player, CallbackInfo ci) {
        OnPlayerLeftEvent event = new OnPlayerLeftEvent(player);
        EventManager.dispatchEvent(OnPlayerLeftEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
