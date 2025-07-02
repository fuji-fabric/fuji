package io.github.sakurawald.fuji.module.mixin.note;

import io.github.sakurawald.fuji.module.initializer.note.NoteInitializer;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        NoteInitializer.processNotify(player, true);
    }

}
