package io.github.sakurawald.module.initializer.echo.send_chat;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;

public class SendChatInitializer extends ModuleInitializer {

    @CommandNode("send-chat")
    @CommandRequirement(level = 4)
    private static int sendChat(ServerPlayerEntity player, GreedyString message) {
        SignedMessage signedMessage = SignedMessage.ofUnsigned(message.getValue());
        player.networkHandler.handleDecoratedMessage(signedMessage);
        return CommandHelper.Return.SUCCESS;
    }

}

