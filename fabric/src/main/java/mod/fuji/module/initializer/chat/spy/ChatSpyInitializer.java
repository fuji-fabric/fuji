package mod.fuji.module.initializer.chat.spy;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerChatMessageSentEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.chat.spy.config.model.ChatSpyConfigModel;
import mod.fuji.module.initializer.chat.spy.config.model.ChatSpyDataModel;
import java.util.function.Function;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826708198L, value = """
    This module allows you to spy on specified chat message type. (Mainly used for private message types)
    """)

@ColorBox(id = 1751899727098L, color = ColorBox.ColorBoxTypes.NOTE, value = """
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
    private static final BaseConfigurationHandler<ChatSpyConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatSpyConfigModel.class);
    private static final BaseConfigurationHandler<ChatSpyDataModel> data = ObjectConfigurationHandler
        .ofModule("data.json", ChatSpyDataModel.class);

    @Document(id = 1751826711342L, value = "Enable/disable the chat spy mode for you.")
    @CommandNode("toggle")
    private static int $toggle(@CommandSource ServerPlayerEntity player) {
        withPlayerOptions(player, true, (playerOptions) -> {
            playerOptions.setEnabled(!playerOptions.isEnabled());
            TextHelper.sendTextByKey(player, playerOptions.isEnabled() ? "on" : "off");
            return null;
        });
        return CommandHelper.Return.SUCCESS;
    }

    private static <T> T withPlayerOptions(@NotNull ServerPlayerEntity player, boolean writeStorage, @NotNull Function<ChatSpyDataModel.PerPlayerOptions, T> function) {
        String key = PlayerHelper.getPlayerName(player);
        var playerOptions = data.model().getOptions().computeIfAbsent(key, k -> new ChatSpyDataModel.PerPlayerOptions());

        T apply = function.apply(playerOptions);
        if (writeStorage) {
            data.writeStorage();
        }
        return apply;
    }

    private static void processChatSpy(@NotNull String messageTypeString, @NotNull ServerPlayerEntity receiverPlayer, @NotNull Text contentText) {
        String contentString = TextHelper.Operators.getString(contentText);
        LogUtil.debug("Process chat spy: message type = {}, content string = {}", messageTypeString, contentString);

        /* Filter by the message types.  */
        if (config.model().getMessageType()
            .getAcceptors()
            .stream()
            .noneMatch(it -> it.matches(messageTypeString))) {
            return;
        }

        /* Make notification text. */
        /* Filter for duplicated chat string. */
        // NOTE: The sent message will be sent to all online players.
        if (config.model().isIgnoreConsecutiveSameText() && contentString.equals(lastContentString)) {
            return;
        }
        lastContentString = contentString;

        Text receiverPlayerName = receiverPlayer.getDisplayName();
        MutableText notificationText = Text.empty();
        notificationText.append(contentText)
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(null, "chat.spy.indicator"))
            .append(TextHelper.TEXT_SPACE)
            .append(receiverPlayerName);

        /* Log the console. */
        if (config.model().isLogConsole()) {
            LogUtil.info(notificationText.getString());
        }

        /* Send the notification. */
        PlayerHelper.Lookup.getOnlinePlayers()
            .stream()
            .filter(it -> withPlayerOptions(it, false, ChatSpyDataModel.PerPlayerOptions::isEnabled))
            .forEach(it -> TextHelper.sendMessageByText(it, notificationText));
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.LOWER)
    private static void consumePlayerChatMessageSentEvent(PlayerChatMessageSentEvent event) {
        /* Extract the message type string. */
        MessageType.Parameters parameters = event.getParameters();
        String messageTypeString = RegistryHelper.getIdAsString(parameters);

        /* Process it. */
        SignedMessage signedMessage = event.getSignedMessage();
        Text contentText = parameters.applyChatDecoration(signedMessage.getContent());
        ChatSpyInitializer.processChatSpy(messageTypeString, event.getReceiverPlayer(), contentText);
    }


}
