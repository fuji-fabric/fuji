package mod.fuji.module.mixin.core.event;

#if MC_VER <= MC_1_20_2
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
#elif MC_VER > MC_1_20_2
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
#endif

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.command.CommandExecutionPostEvent;
import mod.fuji.core.event.message.command.CommandExecutionPreEvent;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@PhasedMixinTemplate
@Mixin(value = CommandDispatcher.class, remap = false)
public class CommandExecutionInCommandDispatcherEventMixin {

    @EventProducer(CommandExecutionPreEvent.class)
    #if MC_VER <= MC_1_20_2
    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/Command;run(Lcom/mojang/brigadier/context/CommandContext;)I"), cancellable = true)
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/context/ContextChain;executeAll(Ljava/lang/Object;Lcom/mojang/brigadier/ResultConsumer;)I"), cancellable = true)
    #endif
    void produceBeforeCommandExecutionInCommandDispatcherEvent(ParseResults<?> parseResults, CallbackInfoReturnable<Integer> cir) {
        CommandHelper.Source.withServerCommandSource(parseResults.getContext(), (serverCommandSource) -> {
            String commandString = parseResults.getReader().getString();
            Optional<Integer> commandReturnValue = Optional.empty();
            CommandExecutionPreEvent event = new CommandExecutionPreEvent(this, serverCommandSource, commandString, cir, commandReturnValue);
            EventManager.dispatchEvent(CommandExecutionPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        });
    }

    @EventProducer(CommandExecutionPostEvent.class)
    #if MC_VER <= MC_1_20_2
    @ModifyExpressionValue(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/Command;run(Lcom/mojang/brigadier/context/CommandContext;)I"))
    int produceAfterCommandExecutionInCommandDispatcherEvent(int original, @Local(argsOnly = true) ParseResults<CommandSourceStack> parseResults)
    #elif MC_VER > MC_1_20_2
    @ModifyReturnValue(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At("RETURN"))
    int produceAfterCommandExecutionInCommandDispatcherEvent(int original, @Local(argsOnly = true) ParseResults<CommandSourceStack> parseResults)
    #endif
    {
        CommandHelper.Source.withServerCommandSource(parseResults.getContext(), (serverCommandSource) -> {
            String commandString = parseResults.getReader().getString();
            Optional<CallbackInfo> callbackInfo = Optional.empty();
            CommandExecutionPostEvent event = new CommandExecutionPostEvent(this, serverCommandSource, commandString, callbackInfo, Optional.of(original));
            EventManager.dispatchEvent(CommandExecutionPostEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        });

        return original;
    }
}
