package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.command.CommandExecutionPostEvent;
import mod.fuji.core.event.message.command.CommandExecutionPreEvent;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
#if MC_VER <= MC_1_20_2
#elif MC_VER > MC_1_20_2
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.command.ReturnValueConsumer;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.ContextChain;
import net.minecraft.command.CommandExecutionContext;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.ParseResults;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
#endif

/**
 *     In MC <= 1.20.2, the CommandManager#execute calls the CommandDispatcher#execute directly.
    In MC > 1.20.2, Mojang introduce the command execution control for game commands, so the CommandManager#execute will not call the CommandDispatcher#execute directly.

 **/
@EventProducer(CommandExecutionPreEvent.class)
@EventProducer(CommandExecutionPostEvent.class)
@PhasedMixinTemplate
@Mixin(value = CommandManager.class)
public class CommandExecutionInCommandManagerEventMixin {

    #if MC_VER <= MC_1_20_2
    // NO-OP Delegates to CommandDispatcher directly.
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"), cancellable = true)
    void produceBeforeCommandExecutionInCommandManagerEvent(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci) {
        ServerCommandSource commandSource = parseResults.getContext().getSource();
        String commandString = parseResults.getReader().getString();
        Optional<Integer> commandReturnValue = Optional.empty();

        CommandExecutionPreEvent event = new CommandExecutionPreEvent(this, commandSource, commandString, ci, commandReturnValue);
        EventManager.dispatchEvent(CommandExecutionPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
    #endif

    #if MC_VER <= MC_1_20_2
    // NO-OP Delegates to CommandDispatcher directly.
    #elif MC_VER > MC_1_20_2
    @SuppressWarnings("CodeBlock2Expr")
    @ModifyArgs(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"))
    void produceAfterCommandExecutionInCommandManagerEvent(Args args, @Local(argsOnly = true) ParseResults<ServerCommandSource> parseResults, @Local(argsOnly = true) String string, @Local ContextChain<ServerCommandSource> contextChain) {
        /* Merge a new return value consumer to capture the target command return value. */
        ServerCommandSource serverCommandSource = args.get(0);
        ReturnValueConsumer returnValueConsumer = makeReturnValueConsumer(parseResults);
        serverCommandSource = serverCommandSource.withReturnValueConsumer(returnValueConsumer);
        args.set(0, serverCommandSource);

        /* Re-capture the latest version of server command source for Consumer<?>. */
        final ServerCommandSource finalServerCommandSource = serverCommandSource;
        final Consumer<CommandExecutionContext<ServerCommandSource>> finalConsumer = commandExecutionContext -> {
            CommandExecutionContext.enqueueCommand(commandExecutionContext, string, contextChain, finalServerCommandSource, ReturnValueConsumer.EMPTY);
        };
        args.set(1, finalConsumer);
    }

    @Unique
    private @NotNull ReturnValueConsumer makeReturnValueConsumer(@NotNull ParseResults<ServerCommandSource> parseResults) {
        return (physicalSuccess, returnValue) -> {
            ServerCommandSource commandSource = parseResults.getContext().getSource();
            String commandString = parseResults.getReader().getString();
            Optional<CallbackInfo> callbackInfo = Optional.empty();
            Optional<Integer> commandReturnValue = Optional.of(returnValue);

            CommandExecutionPostEvent event = new CommandExecutionPostEvent(this, commandSource, commandString, callbackInfo, commandReturnValue);
            EventManager.dispatchEvent(CommandExecutionPostEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        };
    }

    #endif

}
