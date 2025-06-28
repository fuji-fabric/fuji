package io.github.sakurawald.fuji.module.initializer.command_toolbox.help_op;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class HelpOpInitializer extends ModuleInitializer {

    @Document("Send help messages to online ops.")
    @CommandNode("help-op")
    private static int helpop(@CommandSource ServerPlayerEntity player, GreedyString message) {
        PlayerManager playerManager = ServerHelper.getServer().getPlayerManager();
        List<ServerPlayerEntity> ops = playerManager.getPlayerList().stream().filter(p -> playerManager.isOperator(p.getGameProfile())).toList();

        if (ops.isEmpty()) {
            TextHelper.sendMessageByKey(player, "helpop.fail");
            return CommandHelper.Return.FAIL;
        }

        Text text = TextHelper.getTextByKey(player, "helpop.format", player.getGameProfile().getName(), message.getValue());
        ops.forEach(o -> o.sendMessage(text));

        TextHelper.sendMessageByKey(player, "helpop.success");
        return CommandHelper.Return.SUCCESS;
    }

}
