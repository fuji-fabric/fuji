package io.github.sakurawald.module.mixin.command_advice;

import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.module.initializer.command_advice.CommandAdviceInitializer;
import io.github.sakurawald.module.initializer.command_advice.structure.CommandAdviceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, order = CommandAdviceInitializer.KEEP_CLOSER_TO_PRIMARY_METHOD)
    public void beforeExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci) {
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.BEFORE_EXECUTING, ci);
    }

    @Inject(method = "execute", at = @At("TAIL"), cancellable = true, order = CommandAdviceInitializer.KEEP_CLOSER_TO_PRIMARY_METHOD)
    public void afterExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci) {
        CommandAdviceInitializer.processCommandAdvice(this, parseResults.getContext().getSource(), parseResults.getReader().getString(), CommandAdviceType.AFTER_EXECUTING, ci);
    }
}
