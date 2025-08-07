package io.github.sakurawald.fuji.module.initializer.chat.replace;

import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.structure.RegexRewriteNode;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.replace.model.ChatReplaceConfigModel;
import java.util.regex.Pattern;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826656743L, value = """
    This module allows you to replace `chat string` with a given `chat text`. (Including placeholder parsing)
    """)
@ColorBox(id = 1751870539707L, color = ColorBox.ColorBlockTypes.WARNING, value = """
    ◉ This module didn't work with `Styled Chat` mod.
    The `chat.replace` module does work with `chat.style` module.
    But if you are using the `Styled Chat` mod, then it didn't work.
    However, since `Styled Chat` mod allows you to define custom `emotions`.
    So you can `disable` this module, if you are using that mod.
    """)
@ColorBox(id = 1751899554713L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Define a chat shortcut, to evaluate a placeholder.
    Regex: `(?<=^|\\\\s)uuid(?=\\\\s|$)`
    Replacement: `my uuid is %player:uuid%`
    """)
public class ChatReplaceInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatReplaceConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatReplaceConfigModel.class);

    public static Text replaceChatText(@NotNull PlayerEntity player, @NotNull Text text) {
        MutableText ret = text.copy();

        for (RegexRewriteNode rule : config.model().replace.regex) {
            Pattern pattern = rule.getCachedPattern();
            ret = TextHelper.Operators.replaceTextWithPattern(ret, pattern, (matcher) -> {
                String replacement = rule.getReplacement();
                replacement = StringUtil.copyMatcherAndReplaceFirst(pattern, matcher, replacement);
                return TextHelper.getTextByValue(player, replacement);
            });
        }

        LogUtil.debug("Replace chat text: old = {}, new = {}", text, ret);
        return ret;
    }

}
