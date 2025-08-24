package io.github.sakurawald.fuji.module.mixin.command_rewrite;

import io.github.sakurawald.fuji.module.initializer.command_rewrite.CommandRewriteInitializer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
#if MC_VER <= MC_1_20_4
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
#elif MC_VER > MC_1_20_4
import org.spongepowered.asm.mixin.injection.ModifyVariable;
#endif


@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 - 500)
public class ServerPlayNetworkHandlerMixin {

    #if MC_VER <= MC_1_20_4
    @ModifyExpressionValue(method = "handleCommandExecution", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/CommandExecutionC2SPacket;comp_808()Ljava/lang/String;"))
    #elif MC_VER > MC_1_20_4
    @ModifyVariable(method = "executeCommand", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    #endif
    String modifyIssuedCommandString(@NotNull String string) {
         return CommandRewriteInitializer.processCommandRewrite(string);
     }

}
