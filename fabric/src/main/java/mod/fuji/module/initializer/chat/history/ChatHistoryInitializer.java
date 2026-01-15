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
    This module provides the `history` function for vanilla Minecraft's `chat` system.
    """)
@ColorBox(id = 1768404087952L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?

    ➜ Store the `chat text` sent by a player.
    When a player sends a chat text, store it.

    ➜ Restore the `chat texts` to a new joined player.
    When a player joins the server, send all the stored chat texts to the player.
    """)
@ColorBox(id = 1768405649879L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ What is the `acceptors` and `rejectors`?
    A `chat message` = `chat type` + `chat content` + `chat parameter`
    In vanilla Minecraft, all `chat messages` are sent in a shared channel.

    The `acceptors` and `rejectors` are used to `select` the interested `chat message`.
    They are introduced to improve the compatibility with other chat-related mods.

    A `chat type acceptor` filter a `chat message` by its `chat type`.
    A `chat content rejector` filter a `chat message` by its `chat content`.
    A `chat parameter rejector` filter a `chat message` by its `chat parameter`.
    """)
public class ChatHistoryInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatHistoryConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatHistoryConfigModel.class);

    private static Queue<Component> chatHistory;

    @SuppressWarnings("RedundantIfStatement")
    private static boolean isMessageTypeAccepted(@NotNull String messageTypeString) {
        boolean accepted = false;

        /* Filter message types. */
        if (config.model().getMessageTypeAcceptors()
            .stream()
            .anyMatch(messageTypeString::equals)) {
            accepted = true;
        }

        /* Log it. */
        if (!accepted) {
            LogUtil.debug("There is no an acceptor defined to accept the message type {}, ignoring it for the chat history.", messageTypeString);
        }

        return accepted;
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean isMessageRejected(@NotNull String contentString, @NotNull String parametersString) {
        // NOTE: For `Styled Chat` mod user, the parameters.type() is `styled_chat:generic_hack`
        /* NOTE: In vanilla Minecraft, there are many different message types. But styled chat mod use `styled_chat:generic_hack` type for all of them.
        Due to this reason, we can't distinguish the private message and public message.
        **/
        LogUtil.debug("content = {}, parameters = {}", contentString, parametersString);
        boolean rejected = false;

        /* Reject message by content. */
        if (config.model().getMessageRejectors().getContentRejector().getMatches()
            .stream()
            .anyMatch(contentString::matches)) {
            rejected = true;
        }

        /* Reject message by parameters. */
        if (config.model().getMessageRejectors().getParameterRejector().getMatches()
            .stream()
            .anyMatch(parametersString::matches)) {
            rejected = true;
        }

        /* Log it. */
        if (rejected) {
            LogUtil.debug("One defined rejector REJECTS a message, ignoring it for the chat history: content = {}, parameters = {}", contentString, parametersString);
        }

        return rejected;
    }

    private static void processChatHistory(@NotNull PlayerChatMessage signedMessage, @NotNull ChatType.Bound parameters) {
        /* Filter the message by message type. */
        String messageTypeString = RegistryHelper.getIdAsString(parameters);
        if (!isMessageTypeAccepted(messageTypeString)) {
            return;
        }

        /* Filter the message by content and parameters. */
        String contentString = TextHelper.Operators.getString(signedMessage.decoratedContent());
        String parametersString = parameters.toString();
        if (isMessageRejected(contentString, parametersString)) {
            return;
        }

        /* Store the text into the chat history. */
        Component decorateTheTextAsTheClientSideDoes = parameters.decorate(signedMessage.decoratedContent());
        storeChatHistory(decorateTheTextAsTheClientSideDoes);
    }

    @EventConsumer
    private static void initializeChatHistory(@Unused ServerStartedEvent event) {
        chatHistory = EvictingQueue.create(config.model().getMaxChatHistorySize());
    }

    private static void resizeChatHistory() {
        EvictingQueue<Component> newQueue = EvictingQueue.create(config.model().getMaxChatHistorySize());
        newQueue.addAll(chatHistory);
        chatHistory.clear();
        chatHistory = newQueue;
    }

    @Override
    protected void onReload() {
        resizeChatHistory();
    }

    private static void storeChatHistory(@NotNull Component text) {
        chatHistory.add(text);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority = EventConsumer.LOWEST)
    private static void restoreChatHistory(PlayerJoinedEvent event) {
        // NOTE: Use a lower priority, to ensure the chat history is re-played before other welcome messages.
        ServerPlayer player = event.getPlayer();
        chatHistory.forEach(text -> TextHelper.sendMessageByText(player, text));
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void watchSentChatText(PlayerChatMessageSentEvent event) {
        processChatHistory(event.getSignedMessage(), event.getParameters());
    }

}

