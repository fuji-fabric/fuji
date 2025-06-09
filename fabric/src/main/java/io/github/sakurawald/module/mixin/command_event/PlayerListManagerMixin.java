package io.github.sakurawald.module.mixin.command_event;

import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.command_event.CommandEventInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerListManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    void onPlayerJoined(ClientConnection clientConnection, @NotNull ServerPlayerEntity player, ConnectedClientData connectedClientData, CallbackInfo ci) {
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), CommandEventInitializer.config.model().event.on_player_joined.command_list);

        if (player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.LEAVE_GAME)) < 1) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), CommandEventInitializer.config.model().event.on_player_first_joined.command_list);
        }
    }

    @Inject(method = "respawnPlayer", at = @At("TAIL"))
    private void afterRespawn(ServerPlayerEntity oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerPlayerEntity newPlayer = cir.getReturnValue();
        CommandExecutor.execute(ExtendedCommandSource.asConsole(newPlayer.getCommandSource()), CommandEventInitializer.config.model().event.after_player_respawn.command_list);
    }

}
