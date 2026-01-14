package mod.fuji.module.initializer.chat.replace;

import java.util.Optional;
import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerChatMessagePreEvent;
import mod.fuji.core.structure.RegexRewriteNode;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.chat.replace.model.ChatReplaceConfigModel;
import java.util.regex.Pattern;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826656743L, value = """
    This module allows you to replace `chat string` with a given `chat text`. (Including placeholder parsing)
    This module allows replacing `chat strings` with `specified text`, including `placeholder` parsing.
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
    public static Component replaceChatText(@NotNull Player player, @NotNull Component oldText) {
        MutableComponent newText = oldText.copy();

        for (RegexRewriteNode rule : config.model().getReplace().getRules()) {
            Optional<Pattern> pattern = rule.getPattern();
            if (pattern.isEmpty()) continue;
            Pattern $pattern = pattern.get();

            newText = TextHelper.Replacer.replaceTextWithPattern(newText, $pattern, (matcher) -> {
                /* Replace the captured groups. */
                String replacement = rule.getReplacement();
                replacement = StringUtil.copyMatcherAndReplaceFirst($pattern, matcher, replacement);

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
        PlayerChatMessage signedMessage = event.getSignedMessage();

        /* Replace the text. */
        Component oldValue = signedMessage.decoratedContent();
        Component newValue = ChatReplaceInitializer.replaceChatText(event.getPlayer(), oldValue);
        PlayerChatMessage newSignedMessage = signedMessage.withUnsignedContent(newValue);
        event.setSignedMessage(newSignedMessage);
    }

}
