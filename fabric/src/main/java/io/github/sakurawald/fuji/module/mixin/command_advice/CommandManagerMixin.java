package io.github.sakurawald.fuji.module.mixin.command_advice;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
#if MC_VER <= MC_1_20_2
#elif MC_VER > MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.module.initializer.command_advice.CommandAdviceInitializer;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
#endif


@ForDeveloper("""
    In MC <= 1.20.2, the CommandManager#execute calls the CommandDispatcher#execute directly.
    In MC > 1.20.2, Mojang introduce the command execution control for game commands, so the CommandManager#execute will not call the CommandDispatcher#execute directly.
    """)
@Mixin(value = CommandManager.class, priority = 1000 + 1000)
public class CommandManagerMixin {

    #if MC_VER <= MC_1_20_2
    // NO-OP
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"), cancellable = true)
    public void beforeExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    {
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTING, ci);
    }
    #endif

    #if MC_VER <= MC_1_20_2
    // NO-OP
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), cancellable = true)
    public void afterExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    {
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTING, ci);
    }
    #endif
}
