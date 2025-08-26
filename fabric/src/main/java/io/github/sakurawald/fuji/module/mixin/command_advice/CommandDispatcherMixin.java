package io.github.sakurawald.fuji.module.mixin.command_advice;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.module.initializer.command_advice.CommandAdviceInitializer;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceType;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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
    void beforeExecuteInCommandDispatcher(ParseResults<?> parseResults, CallbackInfoReturnable<Integer> cir) {
        CommandHelper.Source.withServerCommandSource(parseResults.getContext(), (serverCommandSource) -> {
            CommandAdviceInitializer.processCommandAdvice(this, serverCommandSource, parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTION, Optional.of(cir), Optional.empty());
        });
    }

    #if MC_VER <= MC_1_20_2
    @WrapOperation(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/Command;run(Lcom/mojang/brigadier/context/CommandContext;)I"))
    int afterExecuteInCommandDispatcher(Command<ServerCommandSource> instance, CommandContext<ServerCommandSource> commandContext, Operation<Integer> original, @Local(argsOnly = true) ParseResults<ServerCommandSource> parseResults)
    #elif MC_VER > MC_1_20_2
    @WrapMethod(method = "execute(Lcom/mojang/brigadier/ParseResults;)I")
    #endif
    int afterExecuteInCommandDispatcher(ParseResults<ServerCommandSource> parseResults, Operation<Integer> original)
    {
        AtomicInteger logicalReturnValue = new AtomicInteger(0);
        CommandHelper.Source.withServerCommandSource(parseResults.getContext(), (serverCommandSource)  -> {

            #if MC_VER <= MC_1_20_2
            logicalReturnValue.set(original.call(instance, commandContext));
            boolean logicalSuccess = CommandHelper.Return.isSuccess(logicalReturnValue);
            CommandAdviceType adviceType = logicalSuccess ? CommandAdviceType.ON_EXECUTION_SUCCESS : CommandAdviceType.ON_EXECUTION_FAILURE;
            CommandAdviceInitializer.processCommandAdvice(this, serverCommandSource, parseResults.getReader().getString(), adviceType, Optional.empty(), Optional.of(logicalReturnValue));
            #elif MC_VER > MC_1_20_2
            logicalReturnValue.set(original.call(parseResults));
            #endif

            CommandAdviceInitializer.processCommandAdvice(this, serverCommandSource, parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTION, Optional.empty(), Optional.of(logicalReturnValue.get()));
        });
        return logicalReturnValue.get();
    }
}
