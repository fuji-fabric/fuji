package io.github.sakurawald.fuji.module.initializer.command_toolbox.disconnect;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DisconnectModuleInitializer extends ModuleInitializer {

    @CommandNode("dis-connect")
    @CommandRequirement(level = 4)
    private static int $disconnect(@CommandSource ServerCommandSource source, @CommandTarget ServerPlayerEntity target, GreedyString reason) {
        Text reasonText = TextHelper.getTextByValue(target, reason.getValue());
        PlayerHelper.disconnectPlayer(target, reasonText);
        return CommandHelper.Return.SUCCESS;
    }

}
