package io.github.sakurawald.module.initializer.chat.spy;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.spy.config.model.ChatSpyConfigModel;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Document("""
    This module allows you to spy on specified chat message type. (Mainly used for private message types)
    """)
@CommandNode("chat spy")
@CommandRequirement(level = 4)
public class ChatSpyInitializer extends ModuleInitializer {

    private static String lastContentString = "";
    public static final BaseConfigurationHandler<ChatSpyConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatSpyConfigModel.class).autoSaveEveryMinute();

    @Document("Enable/disable the chat spy mode for you.")
    @CommandNode("toggle")
    private static int toggle(@CommandSource ServerPlayerEntity player) {
        ChatSpyConfigModel.PerPlayerOptions options = withOptions(player);
        options.enabled = !options.enabled;

        TextHelper.sendMessageByFlag(player, options.enabled);
        return CommandHelper.Return.SUCCESS;
    }

    private static void ensureOptionsExists(ServerPlayerEntity player) {
        String key = player.getGameProfile().getName();
        config.model().getOptions().putIfAbsent(key, new ChatSpyConfigModel.PerPlayerOptions());
    }

    public static ChatSpyConfigModel.PerPlayerOptions withOptions(ServerPlayerEntity player) {
        ensureOptionsExists(player);
        return config.model().getOptions().get(player.getGameProfile().getName());
    }

    public static void processChatSpy(String messageTypeString, ServerPlayerEntity receiverPlayer, SignedMessage signedMessage, MessageType.Parameters parameters) {
        LogUtil.debug("Receive a message with message type {}", messageTypeString);

        /* Filter for whitelisted message types.  */
        if (config.model().message_type.whitelist
            .stream()
            .noneMatch(it -> it.matches(messageTypeString))) {
            return;
        }

        /* Make notification text. */
        Text content = parameters.applyChatDecoration(signedMessage.getContent());
        String contentString = content.getString();

        /* Filter for duplicated chat string. */
        // NOTE: The sent message will be sent to all online players.
        if (config.model().ignore_consecutive_same_text && contentString.equals(lastContentString)) {
            return;
        }
        lastContentString = contentString;

        Text receiverPlayerName = receiverPlayer.getDisplayName();
        MutableText notificationText = Text.empty();
        notificationText.append(content)
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(null, "chat.spy.indicator"))
            .append(TextHelper.TEXT_SPACE)
            .append(receiverPlayerName);

        /* Log the console. */
        if (config.model().log_console) {
            LogUtil.info(notificationText.getString());
        }

        /* Send the notification. */
        ServerHelper.getPlayers()
            .stream()
            .filter(it -> withOptions(it).enabled)
            .forEach(it -> it.sendMessage(notificationText));
    }
}
