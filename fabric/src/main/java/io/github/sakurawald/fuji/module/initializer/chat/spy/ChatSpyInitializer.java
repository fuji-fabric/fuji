package io.github.sakurawald.fuji.module.initializer.chat.spy;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.spy.config.model.ChatSpyConfigModel;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Document(id = 1751826708198L, value = """
    This module allows you to spy on specified chat message type. (Mainly used for private message types)
    """)

@ColorBox(id = 1751899727098L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    Pick a good implementation of `chat spy` in fabric platform is hard.
    In vanilla Minecraft, each chat message has its `message type`.
    You can identify the `private chat` from its `message type`.
    Though not all other chat mods respect this point.
    You can issue `/fuji debug` to enable the `debug mode`.
    And see how the `chat.spy` module works.
    """)

@CommandNode("chat spy")
@CommandRequirement(level = 4)
public class ChatSpyInitializer extends ModuleInitializer {

    private static String lastContentString = "";
    public static final BaseConfigurationHandler<ChatSpyConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatSpyConfigModel.class);

    @Document(id = 1751826711342L, value = "Enable/disable the chat spy mode for you.")
    @CommandNode("toggle")
    private static int $toggle(@CommandSource ServerPlayerEntity player) {
        ChatSpyConfigModel.PerPlayerOptions options = withOptions(player);
        options.enabled = !options.enabled;
        config.writeStorage();

        TextHelper.sendTextByKey(player, options.enabled ? "on" : "off");
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
        PlayerHelper.getOnlinePlayers()
            .stream()
            .filter(it -> withOptions(it).enabled)
            .forEach(it -> it.sendMessage(notificationText));
    }
}
