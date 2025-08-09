package io.github.sakurawald.fuji.module.initializer.echo.send_chat;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;


@Document(id = 1751976535305L, value = """
    This module provides `/send-chat` command.
    To send a `chat message` as a player.
    """)
@ColorBox(id = 1751976268778L, color = ColorBox.ColorBoxTypes.EXAMPLE,value = """
    ◉ Send a chat message as a player.
    Issue: `/send-chat Steve i am steve.`

    ◉ Send a chat message as a player for each online player.
    Issue: `/foreach send-chat %player:name% i am %player:name%`
    """)
public class SendChatInitializer extends ModuleInitializer {

    @CommandNode("send-chat")
    @CommandRequirement(level = 4)
    private static int $sendChat(ServerPlayerEntity player, GreedyString message) {
        SignedMessage signedMessage = SignedMessage.ofUnsigned(message.getValue());
        player.networkHandler.handleDecoratedMessage(signedMessage);
        return CommandHelper.Return.SUCCESS;
    }

}

