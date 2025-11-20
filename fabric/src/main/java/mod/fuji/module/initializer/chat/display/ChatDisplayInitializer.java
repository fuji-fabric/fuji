package mod.fuji.module.initializer.chat.display;

import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.chat.display.config.model.ChatDisplayConfigModel;
import mod.fuji.module.initializer.chat.display.helper.DisplayHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

@Document(id = 1751826642157L, value = """
    This module allows players to show things to others:
    1. Show their main-hand item.
    2. Show their inventory.
    3. Show their ender chest.
    """)
@ColorBox(id = 1759153524537L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Advanced `server-side` show-case mod.
    A new mod is released in https://modrinth.com/mod/showcase
    You may want to check it!
    """)
@ColorBox(id = 1751870533687L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ This module is designed to work with other `chat-related` mods.
    For example, you use this with `Styled Chat` mod.
    """)
@ColorBox(id = 1751870535947L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Define a `chat shortcut` to create a display
    You can use `chat.replace` module, to define chat shortcut.
    The shortcut can be `"item"` for example.
    Then you can type `"item"` in chat.
    To create a display directly using chat, without the commands.
    """)
public class ChatDisplayInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ChatDisplayConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatDisplayConfigModel.class);
    private static final String DISPLAY_TEXT_PLACEHOLDER = "display";

    private static void broadcastDisplayText(ServerPlayer player, String broadcastTextKey, MutableComponent displayText) {
        Component broadcastText = TextHelper.getTextByKey(player, broadcastTextKey);
        broadcastText = TextHelper.Replacer.replaceTextWithNamedArgument(broadcastText, DISPLAY_TEXT_PLACEHOLDER, (matcher) -> displayText);
        TextHelper.sendBroadcastByText(broadcastText);
    }

    @Document(id = 1751826645846L, value = "Show your item in main hand.")
    @CommandNode("chat display item")
    private static int $displayItem(@CommandSource ServerPlayer player) {
        broadcastDisplayText(player, "display.item.broadcast", DisplayHelper.createItemDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826648229L, value = "Show your inventory.")
    @CommandNode("chat display inv")
    private static int $displayInv(@CommandSource ServerPlayer player) {
        broadcastDisplayText(player, "display.inventory.broadcast", DisplayHelper.createInvDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826650468L, value = "Show your enderchest.")
    @CommandNode("chat display ender")
    private static int $displayEnder(@CommandSource ServerPlayer player) {
        broadcastDisplayText(player, "display.ender_chest.broadcast", DisplayHelper.createEnderDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @DocStringProvider(id = 1752000261529L, value = """
        Create a `enderchest display` and return the `clickable` text to open that display.
        """)
    private static void registerDisplayEnderPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("ender", 1752000261529L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, DisplayHelper::createEnderDisplayText);
    }

    @DocStringProvider(id = 1752000274945L, value = """
        Create a `inventory display` and return the `clickable` text to open that display.
        """)
    private static void registerDisplayInvPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("inv", 1752000274945L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, DisplayHelper::createInvDisplayText);
    }


    @DocStringProvider(id = 1752000288451L, value = """
        Create a `item display` and return the `clickable` text to open that display.
        """)
    private static void registerDisplayItemPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("item", 1752000288451L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, DisplayHelper::createItemDisplayText);
    }

    @Override
    protected void registerPlaceholders() {
        registerDisplayItemPlaceholder();
        registerDisplayInvPlaceholder();
        registerDisplayEnderPlaceholder();
    }

}
