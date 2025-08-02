package io.github.sakurawald.fuji.module.mixin.command_advice;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.module.initializer.command_advice.CommandAdviceInitializer;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceType;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CommandDispatcher.class, remap = false, priority = 1000 + 1000)
public class CommandDispatcherMixin {

    #if MC_VER <= MC_1_20_2
    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/Command;run(Lcom/mojang/brigadier/context/CommandContext;)I"), cancellable = true)
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/context/ContextChain;executeAll(Ljava/lang/Object;Lcom/mojang/brigadier/ResultConsumer;)I"), cancellable = true)
    #endif
    public void beforeExecuteInCommandDispatcher(ParseResults<ServerCommandSource> parseResults, CallbackInfoReturnable<Integer> cir) {
        if (CommandHelper.Source.isExecutedOnServerSide(parseResults.getContext())) {
            CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTING, cir);
        }
    }

    #if MC_VER <= MC_1_20_2
    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/Command;run(Lcom/mojang/brigadier/context/CommandContext;)I", shift = At.Shift.AFTER), cancellable = true)
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/context/ContextChain;executeAll(Ljava/lang/Object;Lcom/mojang/brigadier/ResultConsumer;)I", shift = At.Shift.AFTER), cancellable = true)
    #endif
    public void afterExecuteInCommandDispatcher(ParseResults<ServerCommandSource> parseResults, CallbackInfoReturnable<Integer> cir) {
        if (CommandHelper.Source.isExecutedOnServerSide(parseResults.getContext())) {
            CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTING, cir);
        }
    }
}
