package io.github.sakurawald.fuji.module.mixin.command_event;

import io.github.sakurawald.fuji.module.initializer.command_event.CommandEventInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
    private void afterWorldChanged(ServerWorld origin, CallbackInfo ci) {
        var config = CommandEventInitializer.config.model().getEvent().getAfterPlayerChangeWorld();
        if (config.isEnable()) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            CommandEventInitializer.executeCommandOnEvent(player, config.getCommands());
        }
    }
}
