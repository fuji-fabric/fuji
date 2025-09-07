package io.github.sakurawald.fuji.module.initializer.chat.style;

import eu.pb4.placeholders.api.parsers.NodeParser;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.OnPlayerChatMessageEvent;
import io.github.sakurawald.fuji.core.service.style_striper.StyleStriper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.style.model.ChatFormatModel;
import io.github.sakurawald.fuji.module.initializer.chat.style.model.ChatStyleConfigModel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Decoration;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826676414L, value = """
    This module allows you to customize global chat style.
    Besides, players can use `/chat style` to set per-player chat style.
    """)
@ColorBox(id = 1756283844217L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Advanced Chat Style features.
    The `chat.style` module provided by `fuji` is simple.
    If you want advanced chat features, you can use `Styled Chat` mod.
    If you want `/mute`, `/tempmute` commands, you can use `BanHammer` mod.

    <green>TIP: While you are using `Styled Chat` mod, you can still use `chat.*` modules from fuji.
    <green>Most of `chat.*` modules are designed to work with other `chat-related mods`, especially the `Styled Chat` mod.
    """)
@ColorBox(id = 1751870542664L, color = ColorBox.ColorBoxTypes.WARNING, value = """
    If you are using `Styled Chat` mod, then you can `disable` this module.
    Because they provide the same `purpose`.
    """)
@ColorBox(id = 1751870544482L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ The main features of this module:
    1. You can use `style tags` to define complex `format`.
    2. You can define the `global format` for all players.
    3. A player can set its `personal format` using `/chat style` command.
    4. This module is designed to work with other `chat-related` mods.
    5. You can control what style tags a player can use, using permissions.
    """)
@ColorBox(id = 1751870547482L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ You can use `style tags` to write `complex format`.

    See the language grammar here:
    1. https://docs.advntr.dev/minimessage/format.html
    2. https://placeholders.pb4.eu/user/quicktext
    """)
@ColorBox(id = 1751870549047L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ To set the `prefix` and `suffix` for a player.

    You need to install the `luckperms` mod, to provide the `prefix` and `suffix` feature.
    After install it, issue `/lp group default meta setprefix \\<yellow\\>[awesome]` to assign a `prefix`.
    To use the `prefix`, use the placeholder `%fuji:player_prefix%`.
    """)
@ColorBox(id = 1751870550677L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ To set the per-player chat style:
    Issue `/chat style set prefix + %message% + suffix`.
    """)
@ColorBox(id = 1751870552243L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ To allow players to use `\\<blue\\>` tag:
    Issue `/lp group default permission set fuji.style.chat.blue`.
    """)
@ColorBox(id = 1751870553872L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ To allow players to use `\\<b\\>` tag:
    Issue `/lp group default permission set fuji.style.chat.b`.
    """)
@ColorBox(id = 1751870555712L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ To allow players to use all tags:
    All tags also including dangerous tags like `\\<click\\>` tag which can run commands on clicked!
    Issue `/lp group default permission set fuji.style.chat.*`
    """)
@ColorBox(id = 1752175232049L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Customize the chat format using placeholders.
    You may want to enable `placeholder` module, to provide more useful placeholders.
    """)
@CommandNode("chat style")
public class ChatStyleInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatStyleConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatStyleConfigModel.class);

    @ForDeveloper("To avoid the message type already registered in the client-side, and the client-side message type will influence the client-side decorator.")
    public static final RegistryKey<MessageType> MESSAGE_TYPE_KEY = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Identifier.of(Fuji.MOD_ID, "chat_" + StringUtil.toLowerCase(FabricLoader.getInstance().getEnvironmentType().toString())));
    public static final MessageType MESSAGE_TYPE_VALUE = new MessageType(
        Decoration.ofChat("%s%s"),
        Decoration.ofChat("%s%s"));

    private static final BaseConfigurationHandler<ChatFormatModel> chatFormatData = ObjectConfigurationHandler.ofModule("chat.json", ChatFormatModel.class);
    private static final NodeParser CHAT_STYLE_PARSER = TextHelper.Parsers.MINI_MESSAGE_ONLY_PARSER;
    private static final String DEFAULT_CONTENT_FORMAT = "%message%";
    private static final String CHAT_STYLE_TYPE = "chat";

    private static @NotNull String stripeStyleTags(@NotNull PlayerEntity player, @NotNull String chatString) {
        return StyleStriper.stripe(player, CHAT_STYLE_TYPE, chatString);
    }

    @Document(id = 1751826679326L, value = """
        Set your personal chat content format.
        For example: `/chat style set prefix + %message% + suffix`
        """)
    @CommandNode("set")
    private static int $setPerPlayerFormat(@CommandSource @CommandTarget ServerPlayerEntity player, GreedyString format) {
        /* Save the new format. */
        String playerName = PlayerHelper.getPlayerName(player);
        String newFormat = format.getValue();
        newFormat = stripeStyleTags(player, newFormat);
        String stripedFormat = newFormat;
        chatFormatData.model().getFormat().getPlayer2format().put(playerName, newFormat);
        chatFormatData.writeStorage();

        /* Feedback. */
        newFormat = TextHelper.Translator.getLanguageValueByKey(player, "chat.format.set");
        newFormat = newFormat.replace("%s", stripedFormat);
        newFormat = newFormat.replace("%message%", TextHelper.Translator.getLanguageValueByKey(player, "chat.format.show"));
        Text text = TextHelper.getTextByValue(player, newFormat);
        TextHelper.sendMessageByText(player, text);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826681754L, value = "Reset your personal chat content format.")
    @CommandNode("reset")
    private static int $resetPerPlayerFormat(@CommandSource @CommandTarget ServerPlayerEntity player) {
        /* Remove the per-player format. */
        String playerName = PlayerHelper.getPlayerName(player);
        chatFormatData.model().getFormat().getPlayer2format().remove(playerName);
        chatFormatData.writeStorage();

        /* Feedback. */
        TextHelper.sendTextByKey(player, "chat.format.reset");
        return CommandHelper.Return.SUCCESS;
    }

    public static @NotNull Text parseSenderText(@NotNull ServerPlayerEntity player) {
        String senderString = config.model().style.sender;
        return TextHelper.getTextByValue(player, senderString);
    }

    public static @NotNull Text parseContentText(@NotNull ServerPlayerEntity player, String message) {
        String contentString = config.model().style.content.formatted(message);
        String playerName = PlayerHelper.getPlayerName(player);
        contentString = chatFormatData.model().getFormat().getPlayer2format().getOrDefault(playerName, DEFAULT_CONTENT_FORMAT)
            .replace("%message%", contentString);
        contentString = stripeStyleTags(player, contentString);

        return TextHelper.Parsers.parseString(CHAT_STYLE_PARSER, contentString);
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHER)
    private static void handleOnPlayerChatEvent(OnPlayerChatMessageEvent event) {
        /* Get signed message. */
        SignedMessage signedMessage = event.getSignedMessage();

        /* Make sender text. */
        ServerPlayerEntity player = event.getPlayer();
        Text senderText = ChatStyleInitializer.parseSenderText(player);
        MessageType.Parameters newParameters = MessageType.params(ChatStyleInitializer.MESSAGE_TYPE_KEY, ServerHelper.getServer().getRegistryManager(), senderText);
        event.setParameters(newParameters);

        /* Make content text. */
        String contentString = TextHelper.Operators.getString(signedMessage.getContent());
        Text contentText = ChatStyleInitializer.parseContentText(player, contentString);
        SignedMessage newSignedMessage = signedMessage.withUnsignedContent(contentText);
        event.setSignedMessage(newSignedMessage);
    }

}
