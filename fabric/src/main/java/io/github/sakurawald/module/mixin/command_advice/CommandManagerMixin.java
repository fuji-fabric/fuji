package io.github.sakurawald.module.mixin.command_advice;

import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
#if MC_VER <= MC_1_20_2
#elif MC_VER > MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.module.initializer.command_advice.CommandAdviceInitializer;
import io.github.sakurawald.module.initializer.command_advice.structure.CommandAdviceType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
#endif


@Mixin(value = CommandManager.class, priority = 1000 + 1000)
public class CommandManagerMixin {

    #if MC_VER <= MC_1_20_2
    // In MC 1.20.1, to avoid process the advice twice, we only process the advice in CommandDispatcher. (Ignore the CommandManager handler).
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"), cancellable = true)
    public void beforeExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    {
        LogUtil.info("command manager: {}", parseResults.getReader().getString());
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTING, ci);
    }
    #endif

    #if MC_VER <= MC_1_20_2
    // In MC 1.20.1, to avoid process the advice twice, we only process the advice in CommandDispatcher. (Ignore the CommandManager handler).
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), cancellable = true)
    public void afterExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    {
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTING, ci);
    }
    #endif
}
