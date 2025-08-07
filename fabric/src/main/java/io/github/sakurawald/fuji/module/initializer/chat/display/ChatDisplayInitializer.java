package io.github.sakurawald.fuji.module.initializer.chat.display;

import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.display.config.model.ChatDisplayConfigModel;
import io.github.sakurawald.fuji.module.initializer.chat.display.helper.DisplayHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Document(id = 1751826642157L, value = """
    This module allows players to show things to others:
    1. Show their item in main hand.
    2. Show their inventory.
    3. Show their ender chest.
    """)

@ColorBox(id = 1751870533687L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ This module is designed to work with other `chat-related` mods.
    For example, you use this with `Styled Chat` mod.
    """)

@ColorBox(id = 1751870535947L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ Define a `chat shortcut` to create a display
    You can use `chat.replace` module, to define chat shortcut.
    The shortcut can be `"item"` for example.
    Then you can type `"item"` in chat.
    To create a display directly using chat, without the commands.
    """)

public class ChatDisplayInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ChatDisplayConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatDisplayConfigModel.class);
    private static final String DISPLAY_TEXT_PLACEHOLDER = "display";

    private static void broadcastDisplayText(ServerPlayerEntity player, String broadcastTextKey, MutableText displayText) {
        Text broadcastText = TextHelper.getTextByKey(player, broadcastTextKey);
        broadcastText = TextHelper.Operators.replaceTextWithNamedArgument(broadcastText, DISPLAY_TEXT_PLACEHOLDER, (matcher) -> displayText);
        TextHelper.sendBroadcastByText(broadcastText);
    }

    @Document(id = 1751826645846L, value = "Show your item in main hand.")
    @CommandNode("chat display item")
    private static int $displayItem(@CommandSource ServerPlayerEntity player) {
        broadcastDisplayText(player, "display.item.broadcast", DisplayHelper.createItemDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826648229L, value = "Show your inventory.")
    @CommandNode("chat display inv")
    private static int $displayInv(@CommandSource ServerPlayerEntity player) {
        broadcastDisplayText(player, "display.inventory.broadcast", DisplayHelper.createInvDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826650468L, value = "Show your enderchest.")
    @CommandNode("chat display ender")
    private static int $displayEnder(@CommandSource ServerPlayerEntity player) {
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
