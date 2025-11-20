package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.command.CommandExecutionPostEvent;
import mod.fuji.core.event.message.command.CommandExecutionPreEvent;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
#if MC_VER <= MC_1_20_2
#elif MC_VER > MC_1_20_2
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.commands.CommandResultCallback;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.ContextChain;
import net.minecraft.commands.execution.ExecutionContext;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.commands.CommandSourceStack;
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
@Mixin(value = Commands.class)
public class CommandExecutionInCommandManagerEventMixin {

    #if MC_VER <= MC_1_20_2
    // NO-OP Delegates to CommandDispatcher directly.
    #elif MC_VER > MC_1_20_2
    @Inject(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V"), cancellable = true)
    void produceBeforeCommandExecutionInCommandManagerEvent(@NotNull ParseResults<CommandSourceStack> parseResults, String string, CallbackInfo ci) {
        CommandSourceStack commandSource = parseResults.getContext().getSource();
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
    @ModifyArgs(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V"))
    void produceAfterCommandExecutionInCommandManagerEvent(Args args, @Local(argsOnly = true) ParseResults<CommandSourceStack> parseResults, @Local(argsOnly = true) String string, @Local ContextChain<CommandSourceStack> contextChain) {
        /* Merge a new return value consumer to capture the target command return value. */
        CommandSourceStack serverCommandSource = args.get(0);
        CommandResultCallback returnValueConsumer = makeReturnValueConsumer(parseResults);
        serverCommandSource = serverCommandSource.withCallback(returnValueConsumer);
        args.set(0, serverCommandSource);

        /* Re-capture the latest version of server command source for Consumer<?>. */
        final CommandSourceStack finalServerCommandSource = serverCommandSource;
        final Consumer<ExecutionContext<CommandSourceStack>> finalConsumer = commandExecutionContext -> {
            ExecutionContext.queueInitialCommandExecution(commandExecutionContext, string, contextChain, finalServerCommandSource, CommandResultCallback.EMPTY);
        };
        args.set(1, finalConsumer);
    }

    @Unique
    private @NotNull CommandResultCallback makeReturnValueConsumer(@NotNull ParseResults<CommandSourceStack> parseResults) {
        return (physicalSuccess, returnValue) -> {
            CommandSourceStack commandSource = parseResults.getContext().getSource();
            String commandString = parseResults.getReader().getString();
            Optional<CallbackInfo> callbackInfo = Optional.empty();
            Optional<Integer> commandReturnValue = Optional.of(returnValue);

            CommandExecutionPostEvent event = new CommandExecutionPostEvent(this, commandSource, commandString, callbackInfo, commandReturnValue);
            EventManager.dispatchEvent(CommandExecutionPostEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        };
    }

    #endif

}
