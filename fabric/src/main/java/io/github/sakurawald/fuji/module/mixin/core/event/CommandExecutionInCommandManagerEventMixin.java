package io.github.sakurawald.fuji.module.mixin.core.event;

import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.command.AfterCommandExecutionEvent;
import io.github.sakurawald.fuji.core.event.message.command.BeforeCommandExecutionEvent;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.ParseResults;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
#endif

@ForDeveloper("""
    In MC <= 1.20.2, the CommandManager#execute calls the CommandDispatcher#execute directly.
    In MC > 1.20.2, Mojang introduce the command execution control for game commands, so the CommandManager#execute will not call the CommandDispatcher#execute directly.
    """)
@PhasedMixinTemplate
@Mixin(value = CommandManager.class)
public class CommandExecutionInCommandManagerEventMixin {

    #if MC_VER <= MC_1_20_2
    // NO-OP Delegates to CommandDispatcher directly.
    #elif MC_VER > MC_1_20_2
    @EventProducer(BeforeCommandExecutionEvent.class)
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"), cancellable = true)
    void produceBeforeCommandExecutionInCommandManagerEvent(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci) {
        ServerCommandSource commandSource = parseResults.getContext().getSource();
        String commandString = parseResults.getReader().getString();
        Optional<CallbackInfo> callbackInfo = Optional.of(ci);
        Optional<Integer> commandReturnValue = Optional.empty();

        BeforeCommandExecutionEvent event = new BeforeCommandExecutionEvent(this, commandSource, commandString, callbackInfo, commandReturnValue);
        EventManager.dispatchEvent(BeforeCommandExecutionEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
    #endif

    #if MC_VER <= MC_1_20_2
    // NO-OP Delegates to CommandDispatcher directly.
    #elif MC_VER > MC_1_20_2
    @EventProducer(AfterCommandExecutionEvent.class)
    @SuppressWarnings("CodeBlock2Expr")
    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"))
    void produceAfterCommandExecutionInCommandManagerEvent(ServerCommandSource serverCommandSource, Consumer<CommandExecutionContext<ServerCommandSource>> consumer, Operation<Void> original, @Local(argsOnly = true) ParseResults<ServerCommandSource> parseResults, @Local(argsOnly = true) String string, @Local ContextChain<ServerCommandSource> contextChain) {
        /* Merge a new return value consumer to capture the target command return value. */
        final ServerCommandSource finalServerCommandSource = serverCommandSource.withReturnValueConsumer(makeReturnValueConsumer(parseResults));

        /* Replace the original call with new arguments. */
        final Consumer<CommandExecutionContext<ServerCommandSource>> finalConsumer = commandExecutionContext -> {
            CommandExecutionContext.enqueueCommand(commandExecutionContext, string, contextChain, finalServerCommandSource, ReturnValueConsumer.EMPTY);
        };
        original.call(finalServerCommandSource, finalConsumer);
    }

    @Unique
    private @NotNull ReturnValueConsumer makeReturnValueConsumer(@NotNull ParseResults<ServerCommandSource> parseResults) {
        return (physicalSuccess, returnValue) -> {
            ServerCommandSource commandSource = parseResults.getContext().getSource();
            String commandString = parseResults.getReader().getString();
            Optional<CallbackInfo> callbackInfo = Optional.empty();
            Optional<Integer> commandReturnValue = Optional.of(returnValue);

            AfterCommandExecutionEvent event = new AfterCommandExecutionEvent(this, commandSource, commandString, callbackInfo, commandReturnValue);
            EventManager.dispatchEvent(AfterCommandExecutionEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        };
    }

    #endif

}
