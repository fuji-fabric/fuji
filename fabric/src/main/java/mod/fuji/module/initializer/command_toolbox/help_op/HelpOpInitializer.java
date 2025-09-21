package mod.fuji.module.initializer.command_toolbox.help_op;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class HelpOpInitializer extends ModuleInitializer {

    @Document(id = 1751825137661L, value = "Send help messages to online ops.")
    @CommandNode("help-op")
    private static int $helpOp(@CommandSource ServerPlayerEntity player, GreedyString message) {
        List<ServerPlayerEntity> onlineOps = PlayerHelper.Lookup
            .getOnlinePlayers()
            .stream()
            .filter(CommandHelper.Requirement::isOperator)
            .toList();

        if (onlineOps.isEmpty()) {
            TextHelper.sendTextByKey(player, "helpop.fail");
            return CommandHelper.Return.FAILURE;
        }

        String playerName = PlayerHelper.getPlayerName(player);
        Text text = TextHelper.getTextByKey(player, "helpop.format", playerName, message.getValue());
        onlineOps.forEach(op -> TextHelper.sendMessageByText(op, text));

        TextHelper.sendTextByKey(player, "helpop.success");
        return CommandHelper.Return.SUCCESS;
    }

}
