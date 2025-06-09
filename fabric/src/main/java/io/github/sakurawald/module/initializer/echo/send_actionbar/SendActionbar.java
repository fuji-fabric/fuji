package io.github.sakurawald.module.initializer.echo.send_actionbar;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

public class SendActionbar extends ModuleInitializer {

    @CommandNode("send-actionbar")
    @CommandRequirement(level = 4)
    private static int sendActionBar(ServerPlayerEntity player, GreedyString rest) {
        player.sendMessage(TextHelper.getTextByValue(player, rest.getValue()), true);
        return CommandHelper.Return.SUCCESS;
    }

}
