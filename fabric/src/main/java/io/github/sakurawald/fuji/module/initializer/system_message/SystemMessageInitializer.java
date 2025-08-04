package io.github.sakurawald.fuji.module.initializer.system_message;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.system_message.config.model.SystemMessageConfigModel;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Document(id = 1751824913807L, value = """
    Customize the `translatable text`, like most of `system messages`.

    For example, you can customize the `translatable texts` like:
    1. Player joined and left message.
    2. Player death message. (Like get killed by a zombie)
    3. Player whitelist message. (Used in the GUI)
    4. Player advancement message.
    5. Player command feedback. (The command feedback for a specified command)
    6. Server close message.
    7. Player banned screen message.
    8. ... (There are many translatable texts in server-side)
    """)
@ColorBox(id = 1751979387990L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ What is a `translatable text`.
    In client-side, you can change the `language option`, and see different `translatable texts`.
    There are `language files for different languages` in your client.
    Things are similar in server-side.
    There are `language files for different languages` in the `server side`.

    You can see the example `language file` in https://github.com/sakurawald/fuji/blob/dev/.github/files/en_us.json

    ◉ How this module works?
    When the `server` tries to sends a `translatable text` to the `client` side.
    We will see what `translatable text` is going to be sent, and `replace` it with `user-defined text` if needed.
    The client just receive the `user-define text`, and display it to the player.
    """)
@ColorBox(id = 1751979760808L, color = ColorBox.ColorBlockTypes.WARNING, value = """
    ◉ Don't customize the `translatable text` that is already handled by other mods
    For example, if you are using `Styled Chat` mod, then you should not modify the `player join message` and `player leave message`.
    Because the `Styled Chat` mod already handles them.
    """)
@ColorBox(id = 1751979931584L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Customize the player join message
    Key: `multiplayer.player.joined`
    Value: `\\<rainbow\\>+ %s`

    ◉ Customize the player leave mssage
    Key: `multiplayer.player.left`
    Value: `\\<dark_gray\\>%s leeeeeeeeft the game`
    """)
@ColorBox(id = 1751979996501L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Customize a specific death type message
    Key: `death.attack.explosion.player`
    Value: `\\<rainbow\\>%1$s booooooom because of %2$s`

    ◉ Customize a specific vanilla command feedback message
    Key: `commands.seed.success`
    Value: `\\<rainbow\\> Seeeeeeeeeeed: %s`
    """)
@ColorBox(id = 1751980039595L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Customize the text used in screen.
    Key: `multiplayer.disconnect.server_shutdown`
    Value: `Server closeeeeeeeed`

    Key: `multiplayer.disconnect.not_whitelisted`
    Value: `\\<rainbow\\>Please apply a whitelist first!`

    Key: `container.chest`
    Value: `\\<rb\\>I see you opening the chest!`
    """)
@ColorBox(id = 1751980149228L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Cancel the sending of a specific message.
    Key: `multiplayer.player.left`
    Value: `null`

    <green>NOTE: If you leave the `value` to `null`, then it means let's `cancel` the sending of this translatable text.
    """)
@ColorBox(id = 1753460512853L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Customize the `text` in a `screen`.
    Yes, you can customize the `text` in a `screen`, like the `ban screen`.
    Key: `multiplayer.disconnect.banned`
    Value: `\\<red\\>You are banned from this server`

    Key: `multiplayer.disconnect.banned.reason`
    Value: `\\<red\\>You are banned from this server\\<newline\\>\\<yellow\\>Reason: %s`
    """)
public class SystemMessageInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<SystemMessageConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, SystemMessageConfigModel.class);

    public static @Nullable MutableText modifyTranslatableText(String translatableKey, Object... args) {
        Map<String, String> key2value = config.model().rules;
        if (key2value.containsKey(translatableKey)) {
            /* Prevent hijack too early. */
            if (ServerHelper.getServer() == null) {
                LogUtil.warn("Server is null currently, cannot hijack the translatable text with the key: {}", translatableKey);
                return null;
            }

            /* If the value is defined to `null`, then we ignore the modification at this point. And process it at sentMessageToClient(). */
            String value = key2value.get(translatableKey);
            if (value == null) {
                return null;
            }

            /* Replace with a new value. */
            TranslatableTextContent forceFallbackToSpecifiedValue = new TranslatableTextContent("force_fallback", value, args);
            String resolveArgumentsAsString = MutableText
                .of(forceFallbackToSpecifiedValue)
                .getString();
            MutableText newText = TextHelper.getTextByValue(null, resolveArgumentsAsString).copy();
            LogUtil.debug("Replace the translatable text {} with new value.", translatableKey);
            return newText;
        }

        // Return null, that means we will use the original value.
        return null;
    }
}
