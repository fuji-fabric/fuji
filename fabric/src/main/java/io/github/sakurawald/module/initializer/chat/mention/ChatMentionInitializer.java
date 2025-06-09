package io.github.sakurawald.module.initializer.chat.mention;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.mention.config.model.ChatMentionConfigModel;

public class ChatMentionInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<ChatMentionConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatMentionConfigModel.class);
}
