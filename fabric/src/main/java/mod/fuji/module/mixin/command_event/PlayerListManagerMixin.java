package mod.fuji.module.mixin.command_event;

import mod.fuji.module.initializer.command_event.CommandEventInitializer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
#if MC_VER <= MC_1_20_1
#elif MC_VER > MC_1_20_1
import net.minecraft.entity.Entity;
#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerListManagerMixin {

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    #if MC_VER <= MC_1_20_6
    private void afterRespawn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir)
    #elif MC_VER > MC_1_20_6
    private void afterRespawn(ServerPlayerEntity oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir)
    #endif
    {
        var config = CommandEventInitializer.config.model().getEvent().getAfterPlayerRespawn();
        if (config.isEnable()) {
            ServerPlayerEntity newPlayer = cir.getReturnValue();
            CommandEventInitializer.executeCommandOnEvent(newPlayer, config.getCommands());
        }
    }

}
