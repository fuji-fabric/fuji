package mod.fuji.module.mixin.core.event;


import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerJoinedEvent;
import mod.fuji.core.event.message.player.PlayerLeftEvent;
import net.minecraft.network.Connection;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(PlayerList.class)
public class PlayerManagerEventMixin {

    @EventProducer(PlayerJoinedEvent.class)
    #if MC_VER <= MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void produceOnPlayerJoinedEvent(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    void produceOnPlayerJoinedEvent(Connection clientConnection, @NotNull ServerPlayer player, net.minecraft.server.network.CommonListenerCookie connectedClientData, CallbackInfo ci)
    #endif {
        PlayerJoinedEvent event = new PlayerJoinedEvent(player);
        EventManager.dispatchEvent(PlayerJoinedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(PlayerLeftEvent.class)
    @Inject(method = "remove", at = @At("HEAD"))
    void produceOnPlayerLeaveEvent(ServerPlayer player, CallbackInfo ci) {
        PlayerLeftEvent event = new PlayerLeftEvent(player);
        EventManager.dispatchEvent(PlayerLeftEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
