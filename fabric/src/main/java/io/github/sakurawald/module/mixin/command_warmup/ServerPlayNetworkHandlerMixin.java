package io.github.sakurawald.module.mixin.command_warmup;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.structure.Tag;
import io.github.sakurawald.module.initializer.command_warmup.CommandWarmupInitializer;
import io.github.sakurawald.module.initializer.command_warmup.structure.CommandWarmupNode;
import io.github.sakurawald.module.initializer.command_warmup.structure.CommandWarmupTicket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 - 500)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    public void interceptCommandUsagePackets(CommandExecutionC2SPacket commandExecutionC2SPacket, CallbackInfo ci) {
        String commandString = commandExecutionC2SPacket.comp_808();

        /* Iterate the node entries. */
        var config = CommandWarmupInitializer.config.model();
        for (CommandWarmupNode entry : config.entries) {

            /* Test if we should bypass this warmup entry. */
            if (Tag.hasAnyTagPermission(player,"command_warmup.bypass", entry.getTag().getTags())) {
                continue;
            }

            /* If a warmup entry matches the command string, then we cancel the usage of the command. */
            if (commandString.matches(entry.getCommand().getRegex())) {
                // Submit the command warmup ticket.
                Managers.getBossBarManager().addTicket(CommandWarmupTicket.make(player, commandString, entry));

                // Send warning for movement.
                if (config.warn_for_move) {
                    Text text = TextHelper.getTextByKey(player, "command_warmup.warn_for_move", entry.getInterruptible().getInterruptDistance());
                    TextHelper.sendTitle(player, text, Text.empty());
                }

                // Cancel the issue of command string.
                ci.cancel();
                break;
            }
        }

    }
}
