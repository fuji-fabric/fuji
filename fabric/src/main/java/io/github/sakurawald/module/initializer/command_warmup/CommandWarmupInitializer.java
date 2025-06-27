package io.github.sakurawald.module.initializer.command_warmup;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.structure.Tag;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_warmup.config.model.CommandWarmupConfigModel;
import io.github.sakurawald.module.initializer.command_warmup.structure.CommandWarmupNode;
import io.github.sakurawald.module.initializer.command_warmup.structure.CommandWarmupTicket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Document("""
    This module allows you to define a `cooldown` before command execution.
    """)
public class CommandWarmupInitializer extends ModuleInitializer {
    private static final BaseConfigurationHandler<CommandWarmupConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandWarmupConfigModel.class);

    public static void processCommandWarmup(ServerPlayerEntity player, String commandString, CallbackInfo ci) {
        LogUtil.debug("Process command warmup: player = {}, command = {}", PlayerHelper.getName(player), commandString);

        /* Iterate the node entries. */
        var config = CommandWarmupInitializer.config.model();
        for (CommandWarmupNode entry : config.entries) {

            /* Test if we should bypass this warmup entry. */
            if (Tag.hasAnyTagPermission(player,"command_warmup.bypass", entry.getTag().getTags())) {
                continue;
            }

            /* If a warmup entry matches the command string, then we cancel the usage of the command. */
            if (commandString.matches(entry.getCommand().getRegex())) {
                /* Should not send the warmup warning or cancel it, if the player can't even use that command. */
                if (!CommandHelper.canUseThisCommand(player, commandString)) {
                    break;
                }

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
