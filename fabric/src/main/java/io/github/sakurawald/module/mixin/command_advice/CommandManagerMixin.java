package io.github.sakurawald.module.mixin.command_advice;

import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.module.initializer.command_advice.CommandAdviceInitializer;
import io.github.sakurawald.module.initializer.command_advice.structure.CommandAdviceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
#if MC_VER <= MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
#elif MC_VER > MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.NotNull;
#endif


@Mixin(value = CommandManager.class, priority = 1000 + 1000)
public class CommandManagerMixin {

    #if MC_VER <= MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I"), cancellable = true)
    public void beforeExecuteInCommandManager(ParseResults<ServerCommandSource> parseResults, String string, CallbackInfoReturnable<Integer> ci)
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"), cancellable = true)
    public void beforeExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    #endif
    {
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTING, ci);
    }

    #if MC_VER <= MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I", shift = At.Shift.AFTER), cancellable = true)
    public void afterExecuteInCommandManager(ParseResults<ServerCommandSource> parseResults, String string, CallbackInfoReturnable<Integer> ci)
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), cancellable = true)
    public void afterExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    #endif
     {
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTING, ci);
    }
}
