package io.github.sakurawald.fuji.module.mixin.command_spy;

import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER <= MC_1_20_2
#elif MC_VER > MC_1_20_2
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.NotNull;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.module.initializer.command_spy.CommandSpyInitializer;
#endif


@Mixin(CommandManager.class)
public class CommandManagerMixin {

    #if MC_VER <= MC_1_20_2
    // See command advice module.
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At("HEAD"))
    public void onExecuteInCommandManager(@NotNull ParseResults<ServerCommandSource> parseResults, String string, CallbackInfo ci)
    {
        CommandSpyInitializer.processCommandSpy(parseResults);
    }
    #endif
}
