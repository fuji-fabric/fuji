package io.github.sakurawald.module.mixin.world;

import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow
    public abstract ServerChunkManager getChunkManager();

    @Redirect(
        method = "tickWeather",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"
        )
        // this mixin will fail to mixin in neoforge platform.
        , require = 0
    )
    private void dontSendWeatherPacketsToAllWorlds(PlayerManager instance, Packet<?> packet) {
        // Vanilla sends rain packets to all players when rain starts in a world,
        // even if they are not in it, meaning that if it is possible to rain in the world they are in
        // the rain effect will remain until the player changes dimension or reconnects.
        RegistryKey<World> registryKey = this.getChunkManager().getWorld().getRegistryKey();
        instance.sendToDimension(packet, registryKey);
    }
}
