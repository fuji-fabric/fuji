package io.github.sakurawald.module.initializer.chat.style;

import eu.pb4.placeholders.api.parsers.NodeParser;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.service.style_striper.StyleStriper;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.style.model.ChatFormatModel;
import io.github.sakurawald.module.initializer.chat.style.model.ChatStyleConfigModel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Decoration;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Document("""
    This module allows you to customize global chat style.
    Besides, players can use `/chat style` to set per-player chat style.
    """)
@CommandNode("chat style")
public class ChatStyleInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatStyleConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatStyleConfigModel.class);

    // To avoid the message type already registered in the client-side, and the client-side message type will influence the client-side decorator.
    public static final RegistryKey<MessageType> MESSAGE_TYPE_KEY = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Identifier.of(Fuji.MOD_ID, "chat_" + FabricLoader.getInstance().getEnvironmentType().toString().toLowerCase()));

    public static final MessageType MESSAGE_TYPE_VALUE = new MessageType(
        Decoration.ofChat("%s%s"),
        Decoration.ofChat("%s%s"));

    private static final BaseConfigurationHandler<ChatFormatModel> chatFormatData = new ObjectConfigurationHandler<>("chat.json", ChatFormatModel.class);

    private static final NodeParser CHAT_STYLE_PARSER = TextHelper.STYLE_ONLY_PARSER;
    private static final String DEFAULT_CONTENT_FORMAT = "%message%";

    private static final String CHAT_STYLE_TYPE = "chat";

    private static String stripeStyleTags(ServerPlayerEntity player, String string) {
        return StyleStriper.stripe(player, CHAT_STYLE_TYPE, string);
    }

    @Document("Set your personal chat content format.")
    @CommandNode("set")
    private static int setPerPlayerFormat(@CommandSource @CommandTarget ServerPlayerEntity player, GreedyString format) {
        /* Save the new format. */
        String playerName = PlayerHelper.getName(player);
        String newFormat = format.getValue();
        newFormat = stripeStyleTags(player, newFormat);
        String stripedFormat = newFormat;
        chatFormatData.model().format.player2format.put(playerName, newFormat);
        chatFormatData.writeStorage();

        /* Feedback. */
        newFormat = TextHelper.getValueByKey(player, "chat.format.set");
        newFormat = newFormat.replace("%s", stripedFormat);
        newFormat = newFormat.replace("%message%", TextHelper.getValueByKey(player, "chat.format.show"));
        Text text = TextHelper.getTextByValue(player, newFormat);
        player.sendMessage(text);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Reset your personal chat content format.")
    @CommandNode("reset")
    private static int resetPerPlayerFormat(@CommandSource @CommandTarget ServerPlayerEntity player) {
        /* Remove the per-player format. */
        String playerName = PlayerHelper.getName(player);
        chatFormatData.model().format.player2format.remove(playerName);
        chatFormatData.writeStorage();

        /* Feedback. */
        TextHelper.sendMessageByKey(player, "chat.format.reset");
        return CommandHelper.Return.SUCCESS;
    }

    public static @NotNull Text parseSenderText(@NotNull ServerPlayerEntity player) {
        String senderString = config.model().style.sender;
        return TextHelper.getTextByValue(player, senderString);
    }

    public static @NotNull Text parseContentText(@NotNull ServerPlayerEntity player, String message) {
        String contentString = config.model().style.content.formatted(message);
        String playerName = PlayerHelper.getName(player);
        contentString = chatFormatData.model().format.player2format.getOrDefault(playerName, DEFAULT_CONTENT_FORMAT)
            .replace("%message%", contentString);
        contentString = stripeStyleTags(player, contentString);

        return TextHelper.parseString(CHAT_STYLE_PARSER, contentString);
    }

}
