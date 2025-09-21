package mod.fuji.module.initializer.chat.history;

import com.google.common.collect.EvictingQueue;
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
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.chat.history.config.model.ChatHistoryConfigModel;
import java.util.Queue;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826684077L, value = """
    This module will store chat message as history.
    And send them to the player joined the server.
    """)
@ColorBox(id = 1751870560992L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ This module works with `Styled Chat` mod.
    You can use this module to provide the `chat history` for it.
    """)
public class ChatHistoryInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatHistoryConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatHistoryConfigModel.class);

    private static Queue<Text> chatHistory;

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

    private static void processChatHistory(@NotNull SignedMessage signedMessage, @NotNull MessageType.Parameters parameters) {
        /* Filter the message by message type. */
        String messageTypeString = RegistryHelper.getIdAsString(parameters);
        if (!isMessageTypeAccepted(messageTypeString)) {
            return;
        }

        /* Reject the message by content and parameters. (Styled Chat mod will encode info into parameters, and we can detect the feature.) */
        String contentString = TextHelper.Operators.getString(signedMessage.getContent());
        String parametersString = parameters.toString();
        if (isMessageRejected(contentString, parametersString)) {
            return;
        }

        /* Add the message into chat history. */
        Text decoratedTextAsTheClientSideDo = parameters.applyChatDecoration(signedMessage.getContent());
        storeChatHistory(decoratedTextAsTheClientSideDo);
    }

    @Override
    protected void onInitialize() {
        chatHistory = EvictingQueue.create(config.model().getBufferSize());
    }

    @Override
    protected void onReload() {
        EvictingQueue<Text> newQueue = EvictingQueue.create(config.model().getBufferSize());
        newQueue.addAll(chatHistory);
        chatHistory.clear();
        chatHistory = newQueue;
    }

    private static void storeChatHistory(@NotNull Text text) {
        chatHistory.add(text);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority = EventConsumer.LOWEST)
    private static void replayChatHistory(PlayerJoinedEvent event) {
        // NOTE: Use a lower priority, to ensure the chat history is re-played before other welcome messages.
        ServerPlayerEntity player = event.getPlayer();
        chatHistory.forEach(text -> TextHelper.sendMessageByText(player, text));
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void consumePlayerChatMessageSentEvent(PlayerChatMessageSentEvent event) {
        processChatHistory(event.getSignedMessage(), event.getParameters());
    }

}

