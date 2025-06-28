package io.github.sakurawald.fuji.module.initializer.chat.history;

import com.google.common.collect.EvictingQueue;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.history.config.model.ChatHistoryConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Queue;

@Document("""
    This module will store chat message as history.
    And send them to the player joined the server.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.TIPS, value = """
    This module works with `Styled Chat` mod.
    You can use this module to provide the `chat history` for it.
    """)


@SuppressWarnings("UnstableApiUsage")
public class ChatHistoryInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatHistoryConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatHistoryConfigModel.class);

    private static Queue<Text> chatHistory;

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isMessageTypeFiltered(String messageTypeAsString) {
        LogUtil.debug("message type = {}",  messageTypeAsString);
        boolean filtered = false;

        /* Filter message types. */
        if (config.model().message_type_filters
            .stream()
            .anyMatch(messageTypeAsString::equals)) {
            filtered = true;
        }

        /* Log it. */
        if (!filtered) {
            LogUtil.debug("One filter EXCLUDES a message: message type = {}", messageTypeAsString);
        }

        return filtered;
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isMessageRejected(String contentString, String parametersString) {
        // NOTE: For `Styled Chat` mod user, the parameters.type() is `styled_chat:generic_hack`
        /* NOTE: In vanilla Minecraft, there are many different message types. But styled chat mod use `styled_chat:generic_hack` type for all of them.
        Due to this reason, we can't distinguish the private message and public message.
        **/

        LogUtil.debug("content = {}, parameters = {}",  contentString, parametersString);
        boolean rejected = false;

        /* Reject message by content. */
        if (config.model().message_rejectors.content_rejector.rules
            .stream()
            .anyMatch(contentString::contains)) {
            rejected = true;
        }

        /* Reject message by parameters. */
        if (config.model().message_rejectors.parameter_rejector.rules
            .stream()
            .anyMatch(parametersString::contains)) {
            rejected = true;
        }

        /* Log it. */
        if (rejected) {
            LogUtil.debug("One rejector REJECTS a message: content = {}, parameters = {}", contentString, parametersString);
        }

        return rejected;
    }

    @Override
    protected void onInitialize() {
        chatHistory = EvictingQueue.create(config.model().buffer_size);
    }

    @Override
    protected void onReload() {
        EvictingQueue<Text> newQueue = EvictingQueue.create(config.model().buffer_size);
        newQueue.addAll(chatHistory);
        chatHistory.clear();
        chatHistory = newQueue;
    }

    public static void enrichChatHistory(Text text) {
        chatHistory.add(text);
    }

    public static void replayChatHistory(ServerPlayerEntity player) {
        chatHistory.forEach(player::sendMessage);
    }

}

