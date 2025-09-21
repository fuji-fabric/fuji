package io.github.sakurawald.fuji.module.mixin.core.event;


import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerJoinedEvent;
import io.github.sakurawald.fuji.core.event.message.player.PlayerLeftEvent;
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

    @EventProducer(PlayerJoinedEvent.class)
    #if MC_VER <= MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void produceOnPlayerJoinedEvent(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void produceOnPlayerJoinedEvent(ClientConnection clientConnection, @NotNull ServerPlayerEntity player, net.minecraft.server.network.ConnectedClientData connectedClientData, CallbackInfo ci)
    #endif {
        PlayerJoinedEvent event = new PlayerJoinedEvent(player);
        EventManager.dispatchEvent(PlayerJoinedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(PlayerLeftEvent.class)
    @Inject(method = "remove", at = @At("HEAD"))
    void produceOnPlayerLeaveEvent(ServerPlayerEntity player, CallbackInfo ci) {
        PlayerLeftEvent event = new PlayerLeftEvent(player);
        EventManager.dispatchEvent(PlayerLeftEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
