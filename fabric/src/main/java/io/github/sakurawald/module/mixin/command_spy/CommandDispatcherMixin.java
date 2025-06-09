package io.github.sakurawald.module.mixin.command_spy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.module.initializer.command_spy.CommandSpyInitializer;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CommandDispatcher.class, remap = false)
public class CommandDispatcherMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "execute(Lcom/mojang/brigadier/ParseResults;)I", at = @At("HEAD"))
    public void onExecuteInCommandDispatcher(ParseResults<?> parse, CallbackInfoReturnable<Integer> cir) {
        // in client, the S is not guarantee to be ServerCommandSource
        if (parse.getContext().getSource() instanceof ServerCommandSource) {
            CommandSpyInitializer.process((ParseResults<ServerCommandSource>) parse);
        }
    }
}
