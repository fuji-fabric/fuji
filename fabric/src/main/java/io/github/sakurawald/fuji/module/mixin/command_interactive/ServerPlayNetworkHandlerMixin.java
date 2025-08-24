package io.github.sakurawald.fuji.module.mixin.command_interactive;

import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.command_interactive.CommandInteractiveInitializer;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "onCommandExecution", at = @At(value = "HEAD"), cancellable = true)
    void skipTheChatMessageValidationInOnlineModeServer(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        /* Check if it's a trusted packet. */
        if (!CommandInteractiveInitializer.isTrustedPacket(packet)) {
            return;
        }

        /* Cancel the original method call, to skip the chat message validation. */
        CommandInteractiveInitializer.removeTrustedPacket(packet);
        ci.cancel();

        /* Replace the original method with our logics. */
        ServerPlayerEntity player = getPlayer();
        String commandString = packet.comp_808();
        CommandExecutor.executeSingle(ExtendedCommandSource.asPlayer(player.getCommandSource(), player), commandString);
    }

}
