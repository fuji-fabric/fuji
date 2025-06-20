package io.github.sakurawald.module.mixin.command_warmup;

import io.github.sakurawald.module.initializer.command_warmup.CommandWarmupInitializer;
import net.minecraft.network.message.LastSeenMessageList;

#if MC_VER <= MC_1_20_4
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
#elif MC_VER > MC_1_20_4
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
#endif

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 - 500)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    /* MC_VER <= 1.20.4 */
    #if MC_VER < MC_1_20_4
    @Inject(method = "handleCommandExecution", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;execute(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)I"), cancellable = true)
    public void acceptTheCommandExecutionPacketButDoNotSubmitItToCommandManager(CommandExecutionC2SPacket packet, LastSeenMessageList lastSeenMessageList, CallbackInfo ci)
    #elif MC_VER == MC_1_20_4
    @Inject(method = "handleCommandExecution", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;execute(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)V"), cancellable = true)
    public void acceptTheCommandExecutionPacketButDoNotSubmitItToCommandManager(CommandExecutionC2SPacket packet, LastSeenMessageList lastSeenMessageList, CallbackInfo ci)
    #endif
    #if MC_VER <= MC_1_20_4
    {
        String commandString = ServerPlayNetworkHandlerMixin.extractCommandStringFromPacket(packet);
        CommandWarmupInitializer.processCommandWarmup(player, commandString, ci);
    }
    #endif

    /* MC_VER > 1.20.4 */
    // NOTE: There is only 1 call to CommandManager#execute, if MC_VER <= MC 1.20.4
    // NOTE: There are 2 calls to CommandManager#execute, if MC_VER > MC 1.20.4
    #if MC_VER > MC_1_20_4
    @Inject(method = "handleCommandExecution", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;execute(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)V"), cancellable = true)
    public void acceptTheCommandExecutionPacketButDoNotSubmitItToCommandManager(ChatCommandSignedC2SPacket packet, LastSeenMessageList lastSeenMessageList, CallbackInfo ci) {
        String commandString = ServerPlayNetworkHandlerMixin.extractCommandStringFromPacket(packet);
        CommandWarmupInitializer.processCommandWarmup(player, commandString, ci);
    }

    @Inject(method = "executeCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;execute(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)V"), cancellable = true)
    public void acceptTheCommandExecutionPacketButDoNotSubmitItToCommandManager(String string, CallbackInfo ci) {
        CommandWarmupInitializer.processCommandWarmup(player, string, ci);
    }
    #endif

    @Unique
    private static String extractCommandStringFromPacket(
        #if MC_VER <= MC_1_20_4
        CommandExecutionC2SPacket packet
        #elif MC_VER > MC_1_20_4
        ChatCommandSignedC2SPacket packet
        #endif
    ) {
        String commandString;

        #if MC_VER <= MC_1_20_4
        commandString = packet.comp_808();
        #elif MC_VER > MC_1_20_4
        commandString = packet.comp_2532();
        #endif

        return commandString;
    }
}
