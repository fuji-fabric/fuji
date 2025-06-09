package io.github.sakurawald.module.initializer.command_toolbox.help_op;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class HelpOpInitializer extends ModuleInitializer {

    @CommandNode("help-op")
    @Document("Send help messages to online ops.")
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
