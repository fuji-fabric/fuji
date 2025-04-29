package io.github.sakurawald.module.mixin.command_advice;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.module.initializer.command_advice.CommandAdviceInitializer;
import io.github.sakurawald.module.initializer.command_advice.structure.CommandAdviceType;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CommandDispatcher.class, remap = false, priority = 1000 + 1000)
public class CommandDispatcherMixin {

    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/context/ContextChain;executeAll(Ljava/lang/Object;Lcom/mojang/brigadier/ResultConsumer;)I"), cancellable = true)
    public void beforeExecuteInCommandDispatcher(ParseResults<ServerCommandSource> parseResults, CallbackInfoReturnable<Integer> cir) {
        if (parseResults.getContext().getSource() instanceof ServerCommandSource) {
            CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTING, cir);
        }
    }

    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/context/ContextChain;executeAll(Ljava/lang/Object;Lcom/mojang/brigadier/ResultConsumer;)I", shift = At.Shift.AFTER), cancellable = true)
    public void afterExecuteInCommandDispatcher(ParseResults<ServerCommandSource> parseResults, CallbackInfoReturnable<Integer> cir) {
        if (parseResults.getContext().getSource() instanceof ServerCommandSource) {
            CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTING, cir);
        }
    }
}
