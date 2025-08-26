package io.github.sakurawald.fuji.module.mixin.command_advice;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
#if MC_VER <= MC_1_20_2
#elif MC_VER > MC_1_20_2
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.ReturnValueConsumer;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.ContextChain;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import org.spongepowered.asm.mixin.Unique;
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
    void beforeExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    {
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTION, Optional.of(ci), Optional.empty());
    }
    #endif

    #if MC_VER <= MC_1_20_2
    // NO-OP
    #elif MC_VER > MC_1_20_2
    @SuppressWarnings("CodeBlock2Expr")
    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"))
    void afterExecuteInCommandManager(ServerCommandSource serverCommandSource, Consumer<CommandExecutionContext<ServerCommandSource>> consumer, Operation<Void> original, @Local(argsOnly = true) ParseResults<ServerCommandSource> parseResults, @Local(argsOnly = true) String string, @Local ContextChain<ServerCommandSource> contextChain)
    {
        /* Merge a new return value consumer to capture the target command return value. */
        final ServerCommandSource finalServerCommandSource = serverCommandSource.withReturnValueConsumer(makeReturnValueConsumer(parseResults));

        /* Replace the original call with new arguments. */
        final Consumer<CommandExecutionContext<ServerCommandSource>> finalConsumer = commandExecutionContext -> {
            CommandExecutionContext.enqueueCommand(commandExecutionContext, string, contextChain, finalServerCommandSource, ReturnValueConsumer.EMPTY);
        };
        original.call(finalServerCommandSource, finalConsumer);

        /* Process the advices. */
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTION, Optional.empty(), Optional.empty());
    }

    @Unique
    private @NotNull ReturnValueConsumer makeReturnValueConsumer(@NotNull ParseResults<ServerCommandSource> parseResults) {
        return (physicalSuccess, returnValue) -> {
            // NOTE: Use the returnValue to compute the SUCCESS or FAILURE. In some case, the ReturnValueConsumer#onResult may get input like (true, 0).
            boolean logicalSuccess = CommandHelper.Return.isSuccess(returnValue);
            CommandAdviceType adviceType = logicalSuccess ? CommandAdviceType.ON_EXECUTION_SUCCESS : CommandAdviceType.ON_EXECUTION_FAILURE;

            CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), adviceType, Optional.empty(), Optional.of(returnValue));
        };
    }

    #endif
}
