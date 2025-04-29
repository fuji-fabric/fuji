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

@Mixin(value = CommandDispatcher.class, remap = false)
public class CommandDispatcherMixin {

    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At("HEAD"), cancellable = true, order = CommandAdviceInitializer.KEEP_CLOSER_TO_PRIMARY_METHOD)
    public void beforeExecuteInCommandDispatcher(ParseResults<ServerCommandSource> parseResults, CallbackInfoReturnable<Integer> cir) {
        if (parseResults.getContext().getSource() instanceof ServerCommandSource) {
            CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTING, cir);
        }
    }

    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At("TAIL"), cancellable = true, order = CommandAdviceInitializer.KEEP_CLOSER_TO_PRIMARY_METHOD)
    public void afterExecuteInCommandDispatcher(ParseResults<ServerCommandSource> parseResults, CallbackInfoReturnable<Integer> cir) {
        if (parseResults.getContext().getSource() instanceof ServerCommandSource) {
            CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTING, cir);
        }
    }
}
