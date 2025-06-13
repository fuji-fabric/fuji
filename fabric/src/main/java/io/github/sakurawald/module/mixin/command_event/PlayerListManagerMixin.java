package io.github.sakurawald.module.mixin.command_event;

import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.command_event.CommandEventInitializer;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
#if MC_VER <= MC_1_20_1
#elif MC_VER > MC_1_20_1
import org.jetbrains.annotations.NotNull;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ConnectedClientData;
#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerListManagerMixin {

    #if MC_VER <= MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void onPlayerJoined(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void onPlayerJoined(ClientConnection clientConnection, @NotNull ServerPlayerEntity player, ConnectedClientData connectedClientData, CallbackInfo ci)
    #endif
    {
        var onPlayerJoinedConfig = CommandEventInitializer.config.model().event.on_player_joined;
        if (onPlayerJoinedConfig.enable) {
            CommandEventInitializer.executeCommandOnEvent(player, onPlayerJoinedConfig.command_list);
        }

        var onPlayerFirstJoinedConfig = CommandEventInitializer.config.model().event.on_player_first_joined;
        if (onPlayerFirstJoinedConfig.enable) {
            if (player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.LEAVE_GAME)) < 1) {
                CommandEventInitializer.executeCommandOnEvent(player, onPlayerFirstJoinedConfig.command_list);
            }
        }
    }

    @Inject(method = "respawnPlayer", at = @At("TAIL"))
    #if MC_VER <= MC_1_20_6
    private void afterRespawn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir)
    #elif MC_VER > MC_1_20_6
    private void afterRespawn(ServerPlayerEntity oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir)
    #endif
    {
        var config = CommandEventInitializer.config.model().event.after_player_respawn;
        if (config.enable) {
            ServerPlayerEntity newPlayer = cir.getReturnValue();
            CommandEventInitializer.executeCommandOnEvent(newPlayer, config.command_list);
        }
    }

}
