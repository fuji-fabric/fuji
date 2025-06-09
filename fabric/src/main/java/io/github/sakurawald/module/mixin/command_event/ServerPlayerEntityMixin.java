package io.github.sakurawald.module.mixin.command_event;

import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.module.initializer.command_event.CommandEventInitializer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {


    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), CommandEventInitializer.config.model().event.on_player_death.command_list);
    }

    @Inject(method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
    private void afterWorldChanged(ServerWorld origin, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), CommandEventInitializer.config.model().event.after_player_change_world.command_list);
    }
}
