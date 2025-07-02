package io.github.sakurawald.fuji.module.mixin.note;

import io.github.sakurawald.fuji.module.initializer.note.NoteInitializer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

#if MC_VER <= MC_1_20_6
#elif MC_VER > MC_1_20_6
import net.minecraft.network.DisconnectionInfo;
#endif

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At("HEAD"), method = "onDisconnected")
    private void onPlayerLeft(
        #if MC_VER <= MC_1_20_6
        #elif MC_VER > MC_1_20_6
        DisconnectionInfo disconnectionInfo,
        #endif
        CallbackInfo ci) {

        NoteInitializer.processNotify(player, false);
    }
}
