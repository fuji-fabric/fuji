package io.github.sakurawald.module.mixin.command_event;


import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.command_event.CommandEventInitializer;
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
    private void onPlayerLeft(
        #if MC_VER <= MC_1_20_6
        #elif MC_VER > MC_1_20_6
            DisconnectionInfo disconnectionInfo,
        #endif
        CallbackInfo ci) {
        var config = CommandEventInitializer.config.model().event.on_player_left;
        if (config.enable) {
            CommandEventInitializer.executeCommandOnEvent(player, config.command_list);
        }
    }
}
