package io.github.sakurawald.module.mixin.command_cooldown;

import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.command_cooldown.CommandCooldownInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandManager.class, priority = 1000 - 750)
public class CommandManagerMixin {

    #if MC_VER <= MC_1_20_2
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    public void watchCommandExecution(ParseResults<ServerCommandSource> parseResults, String string, CallbackInfoReturnable<Integer> ci)
    #elif MC_VER > MC_1_20_2
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    public void watchCommandExecution(@NotNull ParseResults<ServerCommandSource> parseResults, String string, @NotNull CallbackInfo ci)
    #endif
    {
        ServerPlayerEntity player = parseResults.getContext().getSource().getPlayer();
        if (player == null) return;

        long cooldownMs = CommandCooldownInitializer.computeCooldown(player, string);

        if (cooldownMs > 0) {
            long leftTimeSecond = cooldownMs / 1000;
            Text text = TextHelper.getTextByKey(player, "command_cooldown.cooldown", leftTimeSecond);
            TextHelper.sendTitle(player, text, Text.empty());

            ci.cancel();
        }
    }
}
