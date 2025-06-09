package io.github.sakurawald.module.initializer.chat.history;

import com.google.common.collect.EvictingQueue;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.history.config.model.ChatHistoryConfigModel;
import lombok.Getter;
import net.minecraft.text.Text;

import java.util.Queue;

public class ChatHistoryInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatHistoryConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatHistoryConfigModel.class);

    @Getter
    private static Queue<Text> chatHistory;

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
}

