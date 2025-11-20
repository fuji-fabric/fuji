package mod.fuji.module.mixin.command_event;

import mod.fuji.module.initializer.command_event.CommandEventInitializer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
#if MC_VER <= MC_1_20_1
#elif MC_VER > MC_1_20_1
import net.minecraft.world.entity.Entity;
#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListManagerMixin {

    @Inject(method = "respawn", at = @At("RETURN"))
    #if MC_VER <= MC_1_20_6
    private void afterRespawn(ServerPlayer oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayer> cir)
    #elif MC_VER > MC_1_20_6
    private void afterRespawn(ServerPlayer oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir)
    #endif
    {
        var config = CommandEventInitializer.config.model().getEvent().getAfterPlayerRespawn();
        if (config.isEnable()) {
            ServerPlayer newPlayer = cir.getReturnValue();
            CommandEventInitializer.executeCommandOnEvent(newPlayer, config.getCommands());
        }
    }

}
