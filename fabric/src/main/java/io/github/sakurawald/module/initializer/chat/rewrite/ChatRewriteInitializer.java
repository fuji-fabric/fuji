package io.github.sakurawald.module.initializer.chat.rewrite;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.structure.RegexRewriteNode;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.rewrite.config.model.ChatRewriteConfigModel;

public class ChatRewriteInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ChatRewriteConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatRewriteConfigModel.class);

    public static String rewriteChatString(String string) {
        for (RegexRewriteNode rule : config.model().rewrite.regex) {
            string = string.replaceAll(rule.getRegex(), rule.getReplacement());
        }
        return string;
    }

}
