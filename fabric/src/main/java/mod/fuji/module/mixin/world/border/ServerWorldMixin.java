package mod.fuji.module.mixin.world.border;

import mod.fuji.module.initializer.world.border.WorldBorderInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    @Inject(method = "addPlayer", at = @At("TAIL"))
    void ensureClientSideWorldBorderIsSynced(ServerPlayer player, CallbackInfo ci) {
        // NOTE: addPlayer() will be called from onDimensionChanged(), onPlayerConnected() and onPlayerRespawned()
        Level world = (Level) (Object) this;
        WorldBorderInitializer.sendWorldBorderSyncPacketsToPlayer(player, world);
    }
}
