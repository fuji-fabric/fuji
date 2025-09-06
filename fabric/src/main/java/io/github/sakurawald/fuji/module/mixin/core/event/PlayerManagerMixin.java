package io.github.sakurawald.fuji.module.mixin.core.event;

import io.github.sakurawald.fuji.core.event.message.impl.PlayerEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

#if MC_VER <= MC_1_20_1
#elif MC_VER > MC_1_20_1
import org.jetbrains.annotations.NotNull;
import net.minecraft.server.network.ConnectedClientData;
#endif

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    #if MC_VER <= MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void onPlayerJoined(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void onPlayerJoined(ClientConnection clientConnection, @NotNull ServerPlayerEntity player, ConnectedClientData connectedClientData, CallbackInfo ci)
    #endif
    {
        PlayerEvents.ON_PLAYER_JOINED.invoker().fire(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
        PlayerEvents.ON_PLAYER_LEAVE.invoker().fire(player);
    }

}
