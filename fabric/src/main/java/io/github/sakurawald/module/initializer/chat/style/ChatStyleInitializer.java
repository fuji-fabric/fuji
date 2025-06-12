package io.github.sakurawald.module.initializer.chat.style;

import eu.pb4.placeholders.api.parsers.NodeParser;
#if MC_VER <= MC_1_20_2
import eu.pb4.placeholders.api.parsers.TextParserV1;
#elif MC_VER > MC_1_20_2
#endif

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
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

@CommandNode("chat style")
public class ChatStyleInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ChatStyleConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatStyleConfigModel.class);

    // to avoid the message type already registered in the client-side, and the client-side message type will influence the client-side decorator.
    public static final RegistryKey<MessageType> MESSAGE_TYPE_KEY = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Identifier.of(Fuji.MOD_ID, "chat_" + FabricLoader.getInstance().getEnvironmentType().toString().toLowerCase()));

    public static final MessageType MESSAGE_TYPE_VALUE = new MessageType(
        Decoration.ofChat("%s%s"),
        Decoration.ofChat("%s%s"));

    private static final BaseConfigurationHandler<ChatFormatModel> chat = new ObjectConfigurationHandler<>("chat.json", ChatFormatModel.class);

    private static final NodeParser CHAT_STYLE_PARSER = makeTextParser();

    private static NodeParser makeTextParser() {
        #if MC_VER <= MC_1_20_2
            return TextParserV1.createDefault();
        #elif MC_VER > MC_1_20_2
            return NodeParser.builder()
            .quickText()
            .simplifiedTextFormat()
            .build();
        #endif
    }

    @CommandNode("set")
    private static int setPlayerFormat(@CommandSource @CommandTarget ServerPlayerEntity player, GreedyString format) {
        /* save the format*/
        String name = player.getGameProfile().getName();
        String $format = format.getValue();
        chat.model().format.player2format.put(name, $format);
        chat.writeStorage();

        /* feedback */
        $format = TextHelper.getValueByKey(player, "chat.format.set").replace("%s", $format);
        $format = $format.replace("%message%", TextHelper.getValueByKey(player, "chat.format.show"));
        Text text = CHAT_STYLE_PARSER.parseNode($format).toText();
        player.sendMessage(text);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("reset")
    private static int resetPlayerFormat(@CommandSource @CommandTarget ServerPlayerEntity player) {
        String name = player.getGameProfile().getName();
        chat.model().format.player2format.remove(name);
        chat.writeStorage();
        TextHelper.sendMessageByKey(player, "chat.format.reset");
        return CommandHelper.Return.SUCCESS;
    }


    public static @NotNull Text parseSenderText(@NotNull ServerPlayerEntity player) {
        String senderString = config.model().style.sender;
        return TextHelper.getTextByValue(player, senderString);
    }

    public static @NotNull Text parseContentText(@NotNull ServerPlayerEntity player, String message) {
        String contentString = config.model().style.content.formatted(message);

        contentString = chat.model().format.player2format.getOrDefault(player.getGameProfile().getName(), "%message%").replace("%message%", contentString);

        return CHAT_STYLE_PARSER.parseNode(contentString).toText();
    }

}
