package io.github.sakurawald.module.initializer.chat.replace;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.structure.RegexRewriteNode;
import io.github.sakurawald.core.structure.descriptor.annotation.ColorBox;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.replace.model.ChatReplaceConfigModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Document("""
    This module allows you to replace `chat string` with a given `chat text`. (Including placeholder parsing)
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.WARNING, value = """
    This module didn't work with `Styled Chat` mod.
    The `chat.replace` module does work with `chat.style` module.
    But if you are using the `Styled Chat` mod, then it didn't work.
    However, since `Styled Chat` mod allows you to define custom `emotions`.
    So you can `disable` this module, if you are using that mod.
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
