package io.github.sakurawald.module.initializer.chat.replace;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.structure.RegexRewriteNode;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.replace.model.ChatReplaceConfigModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Document("""
    This module allows you to replace `chat string` with a given `chat text`. (Including placeholder parsing)
    """)
public class ChatReplaceInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatReplaceConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatReplaceConfigModel.class);

    public static Text replaceChatText(PlayerEntity player, Text text) {
        MutableText ret = text.copy();

        for (RegexRewriteNode rule : config.model().replace.regex) {
            String regex = rule.getRegex();
            Text replacement = TextHelper.getTextByValue(player, rule.getReplacement());
            ret = TextHelper.replaceTextWithRegex(ret, regex, () -> replacement);
        }

        LogUtil.debug("Replace chat text: old = {}, new = {}", text, ret);
        return ret;
    }

}
