package mod.fuji.module.initializer.chat.style;

import eu.pb4.placeholders.api.parsers.NodeParser;
import mod.fuji.Fuji;
import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerChatMessagePreEvent;
import mod.fuji.core.service.style_striper.StyleStriper;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.chat.style.model.ChatFormatModel;
import mod.fuji.module.initializer.chat.style.model.ChatStyleConfigModel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826676414L, value = """
    This module allows customization of the `global chat style`.
    In addition, players can use the `/chat style` command to configure their `personal chat style`.
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
@ColorBox(id = 1751870544482L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ The main features of this module:
    1. You can use `style tags` to define complex `format`.
    2. You can define the `global format` for all players.
    3. A player can set its `personal format` using `/chat style` command.
    4. This module is designed to work with other `chat-related` mods.
    5. You can control what style tags a player can use, using permissions.
    """)
@ColorBox(id = 1751870547482L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ You can use `style tags` to write `complex format`.

    See the language grammar here:
    1. https://docs.advntr.dev/minimessage/format.html
    2. https://placeholders.pb4.eu/user/quicktext
    """)
@ColorBox(id = 1751870549047L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ To set the `prefix` and `suffix` for a player.

    You need to install the `luckperms` mod, to provide the `prefix` and `suffix` feature.
    After install it, issue `/lp group default meta setprefix \\<yellow\\>[awesome]` to assign a `prefix`.
    To use the `prefix`, use the placeholder `%fuji:player_prefix%`.

    See: https://luckperms.net/wiki/Prefixes,-Suffixes-&-Meta
    """)
@ColorBox(id = 1751870550677L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ To set the per-player chat style:
    Issue `/chat style set prefix + %message% + suffix`.
    """)
@ColorBox(id = 1751870552243L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ To allow players to use `\\<blue\\>` tag:
    Issue `/lp group default permission set fuji.style.chat.blue`.
    """)
@ColorBox(id = 1751870553872L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ To allow players to use `\\<b\\>` tag:
    Issue `/lp group default permission set fuji.style.chat.b`.
    """)
@ColorBox(id = 1751870555712L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ To allow players to use all tags:
    All tags also including dangerous tags like `\\<click\\>` tag which can run commands on clicked!
    Issue `/lp group default permission set fuji.style.chat.*`
    """)
@ColorBox(id = 1752175232049L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Customize the chat format using placeholders.
    You may want to enable `placeholder` module, to provide more useful placeholders.
    """)
@CommandNode("chat style")
public class ChatStyleInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatStyleConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatStyleConfigModel.class);

    /**
     * To avoid the message type already registered in the client-side, and the client-side message type will influence the client-side decorator.
     **/
    private static final IdentifierIR MESSAGE_TYPE_ID = IdentifierIR.makeIdentifierOrThrow(Fuji.MOD_ID, "chat_" + StringUtil.toLowerCase(FabricLoader.getInstance().getEnvironmentType().toString()));
    public static final ResourceKey<ChatType> MESSAGE_TYPE_KEY = ResourceKey.create(Registries.CHAT_TYPE, MESSAGE_TYPE_ID.getNativeValue());
    public static final ChatType MESSAGE_TYPE_VALUE = new ChatType(
        ChatTypeDecoration.withSender("%s%s"),
        ChatTypeDecoration.withSender("%s%s"));

    private static final BaseConfigurationHandler<ChatFormatModel> chatFormatData = ObjectConfigurationHandler.ofModule("chat.json", ChatFormatModel.class);
    private static final NodeParser CHAT_STYLE_PARSER = TextHelper.Parsers.MINI_MESSAGE_ONLY_PARSER;
    private static final String DEFAULT_CONTENT_FORMAT = "%message%";
    private static final String CHAT_STYLE_TYPE = "chat";

    private static @NotNull String stripeStyleTags(@NotNull Player player, @NotNull String chatString) {
        return StyleStriper.stripe(player, CHAT_STYLE_TYPE, chatString);
    }

    @Document(id = 1751826679326L, value = """
        Set your personal chat content format.
        For example: `/chat style set prefix + %message% + suffix`
        """)
    @CommandNode("set")
    private static int $setPerPlayerFormat(@CommandSource @CommandTarget ServerPlayer player, GreedyString format) {
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
        Component text = TextHelper.getTextByValue(player, newFormat);
        TextHelper.sendMessageByText(player, text);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826681754L, value = "Reset your personal chat content format.")
    @CommandNode("reset")
    private static int $resetPerPlayerFormat(@CommandSource @CommandTarget ServerPlayer player) {
        /* Remove the per-player format. */
        String playerName = PlayerHelper.getPlayerName(player);
        chatFormatData.model().getFormat().getPlayer2format().remove(playerName);
        chatFormatData.writeStorage();

        /* Feedback. */
        TextHelper.sendTextByKey(player, "chat.format.reset");
        return CommandHelper.Return.SUCCESS;
    }

    public static @NotNull Component parseSenderText(@NotNull ServerPlayer player) {
        String senderString = config.model().style.sender;
        return TextHelper.getTextByValue(player, senderString);
    }

    public static @NotNull Component parseContentText(@NotNull ServerPlayer player, String message) {
        String contentString = config.model().style.content.formatted(message);
        String playerName = PlayerHelper.getPlayerName(player);
        contentString = chatFormatData.model().getFormat().getPlayer2format().getOrDefault(playerName, DEFAULT_CONTENT_FORMAT)
            .replace("%message%", contentString);
        contentString = stripeStyleTags(player, contentString);

        return TextHelper.Parsers.parseString(CHAT_STYLE_PARSER, contentString);
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHER)
    private static void handleOnPlayerChatEvent(PlayerChatMessagePreEvent event) {
        /* Get signed message. */
        PlayerChatMessage signedMessage = event.getSignedMessage();

        /* Make sender text. */
        ServerPlayer player = event.getPlayer();
        Component senderText = ChatStyleInitializer.parseSenderText(player);
        ChatType.Bound newParameters = ChatType.bind(ChatStyleInitializer.MESSAGE_TYPE_KEY, ServerHelper.getServer().registryAccess(), senderText);
        event.setParameters(newParameters);

        /* Make content text. */
        String contentString = TextHelper.Operators.getString(signedMessage.decoratedContent());
        Component contentText = ChatStyleInitializer.parseContentText(player, contentString);
        PlayerChatMessage newSignedMessage = signedMessage.withUnsignedContent(contentText);
        event.setSignedMessage(newSignedMessage);
    }

}
