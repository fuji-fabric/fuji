package mod.fuji.module.initializer.chat.history;

import com.google.common.collect.EvictingQueue;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerChatMessageSentEvent;
import mod.fuji.core.event.message.player.PlayerJoinedEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.chat.history.config.model.ChatHistoryConfigModel;
import java.util.Queue;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826684077L, value = """
    This module does:
    1. Stores chat messages as history.
    2. Delivers them to players when they join the server.
    """)
@ColorBox(id = 1751870560992L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ This module works with `Styled Chat` mod.
    You can use this module to provide the `chat history` for it.
    """)
public class ChatHistoryInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatHistoryConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatHistoryConfigModel.class);

    private static Queue<Component> chatHistory;

    @SuppressWarnings("RedundantIfStatement")
    private static boolean isMessageTypeAccepted(@NotNull String messageTypeAsString) {
        boolean acceptedMessageType = false;

        /* Filter message types. */
        if (config.model().getMessageTypeAcceptors()
            .stream()
            .anyMatch(messageTypeAsString::equals)) {
            acceptedMessageType = true;
        }

        /* Log it. */
        if (!acceptedMessageType) {
            LogUtil.debug("There is no an acceptor defined to accept the message type {}, ignoring it for the chat history.", messageTypeAsString);
        }

        return acceptedMessageType;
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean isMessageRejected(@NotNull String contentString, @NotNull String parametersString) {
        // NOTE: For `Styled Chat` mod user, the parameters.type() is `styled_chat:generic_hack`
        /* NOTE: In vanilla Minecraft, there are many different message types. But styled chat mod use `styled_chat:generic_hack` type for all of them.
        Due to this reason, we can't distinguish the private message and public message.
        **/
        LogUtil.debug("content = {}, parameters = {}", contentString, parametersString);

        boolean rejectedMessage = false;

        /* Reject message by content. */
        if (config.model().getMessageRejectors().getContentRejector().getMatches()
            .stream()
            .anyMatch(contentString::matches)) {
            rejectedMessage = true;
        }

        /* Reject message by parameters. */
        if (config.model().getMessageRejectors().getParameterRejector().getMatches()
            .stream()
            .anyMatch(parametersString::matches)) {
            rejectedMessage = true;
        }

        /* Log it. */
        if (rejectedMessage) {
            LogUtil.debug("One defined rejector REJECTS a message, ignoring it for the chat history: content = {}, parameters = {}", contentString, parametersString);
        }

        return rejectedMessage;
    }

    private static void processChatHistory(@NotNull PlayerChatMessage signedMessage, @NotNull ChatType.Bound parameters) {
        /* Filter the message by message type. */
        String messageTypeString = RegistryHelper.getIdAsString(parameters);
        if (!isMessageTypeAccepted(messageTypeString)) {
            return;
        }

        /* Reject the message by content and parameters. (Styled Chat mod will encode info into parameters, and we can detect the feature.) */
        String contentString = TextHelper.Operators.getString(signedMessage.decoratedContent());
        String parametersString = parameters.toString();
        if (isMessageRejected(contentString, parametersString)) {
            return;
        }

        /* Add the message into chat history. */
        Component decoratedTextAsTheClientSideDo = parameters.decorate(signedMessage.decoratedContent());
        storeChatHistory(decoratedTextAsTheClientSideDo);
    }

    @EventConsumer
    private static void onServerStarted(@Unused ServerStartedEvent event) {
        chatHistory = EvictingQueue.create(config.model().getBufferSize());
    }

    @Override
    protected void onReload() {
        EvictingQueue<Component> newQueue = EvictingQueue.create(config.model().getBufferSize());
        newQueue.addAll(chatHistory);
        chatHistory.clear();
        chatHistory = newQueue;
    }

    private static void storeChatHistory(@NotNull Component text) {
        chatHistory.add(text);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority = EventConsumer.LOWEST)
    private static void replayChatHistory(PlayerJoinedEvent event) {
        // NOTE: Use a lower priority, to ensure the chat history is re-played before other welcome messages.
        ServerPlayer player = event.getPlayer();
        chatHistory.forEach(text -> TextHelper.sendMessageByText(player, text));
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void consumePlayerChatMessageSentEvent(PlayerChatMessageSentEvent event) {
        processChatHistory(event.getSignedMessage(), event.getParameters());
    }

}

