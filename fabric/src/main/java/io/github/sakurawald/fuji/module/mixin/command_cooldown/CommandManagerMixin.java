package io.github.sakurawald.fuji.module.mixin.command_cooldown;

import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.service.UnnamedCooldownService;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

#if MC_VER <= MC_1_20_2
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
#elif MC_VER > MC_1_20_2
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
#endif


@Mixin(value = CommandManager.class, priority = 1000 - 750)
public class CommandManagerMixin {

    // NOTE: The `/run as player <player> back` submits the command directly into CommandDispatcher.
    // NOTE: For command cooldown module, we inject on CommandManager, so that other internal command execution will bypass the command cooldown facility.
    // NOTE: It's okay to use ci.cancel() for a CallbackInfoReturnable type.

    #if MC_VER <= MC_1_20_2
    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    public void watchCommandExecution(ParseResults<ServerCommandSource> parseResults, String string, CallbackInfoReturnable<Integer> ci)
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    public void watchCommandExecution(@NotNull ParseResults<ServerCommandSource> parseResults, String string, @NotNull CallbackInfo ci)
    #endif
    {
        /* The command cooldown only applied for player command source. */
        CommandHelper.Source.withServerPlayerEntity(parseResults.getContext(), player -> {
            /* Compute the cooldown for specified command. */
            long remainingDuration = UnnamedCooldownService.computeRemainingUnnamedCooldownDuration(player, string);
            if (remainingDuration > 0) {
                // NOTE: For unnamed cooldown type, the `second unit` is sufficient for use.
                long remainingDurationInSecond = remainingDuration / 1000;
                TextHelper.sendTextByKey(player, "command_cooldown.cooldown", remainingDurationInSecond);
                ci.cancel();
            }
        });
    }
}
