package io.github.sakurawald.module.mixin.command_spy;

import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.module.initializer.command_spy.CommandSpyInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

#if MC_VER <= MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
#elif MC_VER > MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.NotNull;
#endif


@Mixin(CommandManager.class)
public class CommandManagerMixin {

    @Inject(method = "execute", at = @At("HEAD"))
    #if MC_VER <= MC_1_20_2
    public void onExecuteInCommandManager(ParseResults<ServerCommandSource> parseResults, String string, CallbackInfoReturnable<Integer> cir)
    #elif MC_VER > MC_1_20_2
    public void onExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    #endif
    {
        CommandSpyInitializer.process(parseResults);
    }
}
