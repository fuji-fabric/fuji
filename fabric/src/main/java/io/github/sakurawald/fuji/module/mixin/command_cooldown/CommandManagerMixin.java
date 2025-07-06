package io.github.sakurawald.fuji.module.mixin.command_cooldown;

import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.CommandCooldownInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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

    // NOTE: The `/run as player` submits the command directly into CommandDispatcher.
    // NOTE: For Command cooldown, we inject on CommandManager, so that other internal command execution will bypass the command cooldown.
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
        /* The command cooldown only works for player command source. */
        ServerPlayerEntity player = parseResults.getContext().getSource().getPlayer();
        if (player == null) return;

        /* Compute the cooldown for specified command. */
        long cooldownMs = CommandCooldownInitializer.computeCooldown(player, string);
        if (cooldownMs > 0) {
            long leftTimeSecond = cooldownMs / 1000;
            Text text = TextHelper.getTextByKey(player, "command_cooldown.cooldown", leftTimeSecond);
            TextHelper.Sender.sendTitleByText(player, text, Text.empty());

            ci.cancel();
        }
    }
}
