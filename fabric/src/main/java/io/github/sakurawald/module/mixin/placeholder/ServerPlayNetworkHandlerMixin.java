package io.github.sakurawald.module.mixin.placeholder;


import io.github.sakurawald.module.initializer.placeholder.structure.SumUpPlaceholder;
import net.minecraft.network.DisconnectionInfo;
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
    private void removeSumUpPlaceholderToAvoidMemoryLeak(DisconnectionInfo disconnectionInfo, CallbackInfo ci) {
        SumUpPlaceholder.uuid2stats.remove(player.getUuidAsString());
    }
}
