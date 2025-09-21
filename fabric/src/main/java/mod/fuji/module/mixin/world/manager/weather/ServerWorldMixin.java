package mod.fuji.module.mixin.world.manager.weather;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow
    public abstract ServerChunkManager getChunkManager();

    @WrapOperation(
        method = "tickWeather",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"
        )
        // This mixin will fail to mixin in neoforge platform.
        , require = 0
    )
    private void dontSendWeatherPacketsToAllWorlds(PlayerManager instance, Packet<?> packet, Operation<Void> original) {
        // Vanilla sends rain packets to all players when rain starts in a world,
        // even if they are not in it, meaning that if it is possible to rain in the world they are in
        // the rain effect will remain until the player changes dimension or reconnects.
        RegistryKey<World> registryKey = this.getChunkManager().getWorld().getRegistryKey();
        instance.sendToDimension(packet, registryKey);
    }
}
