package mod.fuji.module.mixin.world.manager.weather;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin {

    @Shadow
    public abstract ServerChunkCache getChunkSource();

    @WrapOperation(
        method = "advanceWeatherCycle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
        )
        // This mixin will fail to mixin in neoforge platform.
        , require = 0
    )
    private void dontSendWeatherPacketsToAllWorlds(PlayerList instance, Packet<?> packet, Operation<Void> original) {
        // Vanilla sends rain packets to all players when rain starts in a world,
        // even if they are not in it, meaning that if it is possible to rain in the world they are in
        // the rain effect will remain until the player changes dimension or reconnects.
        ResourceKey<Level> registryKey = this.getChunkSource().getLevel().dimension();
        instance.broadcastAll(packet, registryKey);
    }
}
