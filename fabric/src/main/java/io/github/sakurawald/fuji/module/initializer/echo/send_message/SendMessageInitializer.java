package io.github.sakurawald.fuji.module.initializer.echo.send_message;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Document(id = 1751975862231L, value = """
    This module provides the `/send-message` command.
    To send the `text` as `message` to a specified player.
    """)
@ColorBox(id = 1751976017118L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Say hello to a player
    Issue: `/send-message Alice \\<blue\\>Hello %player:name%`
    """)
public class SendMessageInitializer extends ModuleInitializer {

    @CommandNode("send-message")
    @CommandRequirement(level = 4)
    private static int $sendMessage(ServerPlayerEntity player, GreedyString rest) {
        Text text = TextHelper.getTextByValue(player, rest.getValue());
        TextHelper.sendText(player, text, TextHelper.Sender.TextLocation.MESSAGE);
        return CommandHelper.Return.SUCCESS;
    }

}
