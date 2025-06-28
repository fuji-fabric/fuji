package io.github.sakurawald.fuji.module.mixin.placeholder;


import io.github.sakurawald.fuji.module.initializer.placeholder.structure.SumUpPlaceholder;
#if MC_VER <= MC_1_20_6
#elif MC_VER > MC_1_20_6
import net.minecraft.network.DisconnectionInfo;
#endif
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At("HEAD"), method = "onDisconnected")
    private void removeSumUpPlaceholderToAvoidMemoryLeak(
        #if MC_VER <= MC_1_20_6
        #elif MC_VER > MC_1_20_6
        DisconnectionInfo disconnectionInfo,
        #endif
        CallbackInfo ci) {
        SumUpPlaceholder.uuid2stats.remove(player.getUuidAsString());
    }
}
