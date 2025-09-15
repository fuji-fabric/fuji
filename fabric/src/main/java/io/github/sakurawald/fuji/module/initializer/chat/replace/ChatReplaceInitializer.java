package io.github.sakurawald.fuji.module.initializer.chat.replace;

import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerChatMessagePreEvent;
import io.github.sakurawald.fuji.core.structure.RegexRewriteNode;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.replace.model.ChatReplaceConfigModel;
import java.util.regex.Pattern;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826656743L, value = """
    This module allows you to replace `chat string` with a given `chat text`. (Including placeholder parsing)
    """)
@ColorBox(id = 1751870539707L, color = ColorBox.ColorBoxTypes.WARNING, value = """
    ◉ This module didn't work with `Styled Chat` mod.
    The `chat.replace` module does work with `chat.style` module.
    But if you are using the `Styled Chat` mod, then it didn't work.
    However, since `Styled Chat` mod allows you to define custom `emotions`.
    So you can `disable` this module, if you are using that mod.
    """)
@ColorBox(id = 1751899554713L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Define a chat shortcut, to evaluate a placeholder.
    Regex: `(?<=^|\\\\s)uuid(?=\\\\s|$)`
    Replacement: `my uuid is %player:uuid%`
    """)
public class ChatReplaceInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatReplaceConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatReplaceConfigModel.class);

    @TestCase(action = "Test the `chat replace` and `chat trigger` module.", targets = {
         "Input `inv`"
        , "Input `prefix inv`"
        , "Input `prefix inv<space>`"
        , "Input `inv suffix`"
        , "Input `prefix inv item ender suffix inv suffix`"
        , "Input `prefix prefix item`"
    })
    public static Text replaceChatText(@NotNull PlayerEntity player, @NotNull Text oldText) {
        MutableText newText = oldText.copy();

        for (RegexRewriteNode rule : config.model().getReplace().getRules()) {
            Pattern cachedPattern = rule.getCachedPattern();
            newText = TextHelper.Replacer.replaceTextWithPattern(newText, cachedPattern, (matcher) -> {
                /* Replace the captured groups. */
                String replacement = rule.getReplacement();
                replacement = StringUtil.copyMatcherAndReplaceFirst(cachedPattern, matcher, replacement);

                /* Parse the placeholders. */
                return TextHelper.getTextByValue(player, replacement);
            });
        }

        LogUtil.debug("Replace chat text: old = {}, new = {}", oldText, newText);
        return newText;
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST)
    private static void handleOnPlayerChatEvent(PlayerChatMessagePreEvent event) {
        /* Get signed message. */
        SignedMessage signedMessage = event.getSignedMessage();

        /* Replace the text. */
        Text oldValue = signedMessage.getContent();
        Text newValue = ChatReplaceInitializer.replaceChatText(event.getPlayer(), oldValue);
        SignedMessage newSignedMessage = signedMessage.withUnsignedContent(newValue);
        event.setSignedMessage(newSignedMessage);
    }

}
