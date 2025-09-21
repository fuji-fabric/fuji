package mod.fuji.module.mixin.world.border;

import mod.fuji.module.initializer.world.border.WorldBorderInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(method = "addPlayer", at = @At("TAIL"))
    void ensureClientSideWorldBorderIsSynced(ServerPlayerEntity player, CallbackInfo ci) {
        // NOTE: addPlayer() will be called from onDimensionChanged(), onPlayerConnected() and onPlayerRespawned()
        World world = (World) (Object) this;
        WorldBorderInitializer.sendWorldBorderSyncPacketsToPlayer(player, world);
    }
}
