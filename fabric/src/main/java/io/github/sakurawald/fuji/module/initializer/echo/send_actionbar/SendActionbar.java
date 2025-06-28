package io.github.sakurawald.fuji.module.initializer.echo.send_actionbar;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

public class SendActionbar extends ModuleInitializer {

    @CommandNode("send-actionbar")
    @CommandRequirement(level = 4)
    private static int sendActionBar(ServerPlayerEntity player, GreedyString rest) {
        player.sendMessage(TextHelper.getTextByValue(player, rest.getValue()), true);
        return CommandHelper.Return.SUCCESS;
    }

}
