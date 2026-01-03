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
import mod.fuji.module.initializer.chat.display.service.ChatDisplayService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826642157L, value = """
    This module allows a player to `show` things to `others`.
    The `thing` can be:
    1. Their main-hand item.
    2. Their inventory.
    3. Their ender chest.
    """)
@ColorBox(id = 1759153524537L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Advanced `server-side` show-case mod.
    A new mod is released in https://modrinth.com/mod/showcase
    You may want to check it!
    """)
@ColorBox(id = 1751870535947L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Integrate this module with other `chat-related` mods.

    ➜ Integrate via the `placeholder` way.
    You can use `chat.replace` module.
    That module can be used to define a `chat text` replacement.
    To replace the chat text from `item` to the `%fuji:item%` placeholder.
    To replace the chat text from `inv` to the `%fuji:inv%` placeholder.
    To replace the chat text from `ender` to the `%fuji:ender%` placeholder.

    ➜ Integrate via the `command` way.
    You can use `chat.trigger` module.
    That module can be used to define a `chat string` trigger.
    To execute the `/run as fake-op %player:name% chat display item` command, when the `trigger` is `fired`.
    To execute the `/run as fake-op %player:name% chat display inv` command, when the `trigger` is `fired`.
    To execute the `/run as fake-op %player:name% chat display ender` command, when the `trigger` is `fired`.
    """)
public class ChatDisplayInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ChatDisplayConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatDisplayConfigModel.class);

    private static final String DISPLAY_TEXT_NAMED_ARGUMENT_NAME = "display";

    private static void broadcastDisplayText(@NotNull ServerPlayer sharingPlayer, @NotNull String broadcastTextKey, @NotNull Component displayText) {
        Component broadcastText = TextHelper.getTextByKey(sharingPlayer, broadcastTextKey);
        broadcastText = TextHelper.Replacer.replaceTextWithNamedArgument(broadcastText, DISPLAY_TEXT_NAMED_ARGUMENT_NAME, (matcher) -> displayText);
        TextHelper.sendBroadcastByText(broadcastText);
    }

    @Document(id = 1751826645846L, value = "Show your item in main hand to others.")
    @CommandNode("chat display item")
    private static int $displayItem(@CommandSource ServerPlayer player) {
        broadcastDisplayText(player, "display.item.broadcast", ChatDisplayService.createItemDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826648229L, value = "Show your inventory to others.")
    @CommandNode("chat display inv")
    private static int $displayInv(@CommandSource ServerPlayer player) {
        broadcastDisplayText(player, "display.inventory.broadcast", ChatDisplayService.createInvDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826650468L, value = "Show your enderchest to others.")
    @CommandNode("chat display ender")
    private static int $displayEnder(@CommandSource ServerPlayer player) {
        broadcastDisplayText(player, "display.ender_chest.broadcast", ChatDisplayService.createEnderDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void registerPlaceholders() {
        registerDisplayItemPlaceholder();
        registerDisplayInvPlaceholder();
        registerDisplayEnderPlaceholder();
    }

    @DocStringProvider(id = 1752000261529L, value = """
        Create a `enderchest display` and return the `clickable` text to open that display.
        """)
    private static void registerDisplayEnderPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("ender", 1752000261529L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, ChatDisplayService::createEnderDisplayText);
    }

    @DocStringProvider(id = 1752000274945L, value = """
        Create a `inventory display` and return the `clickable` text to open that display.
        """)
    private static void registerDisplayInvPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("inv", 1752000274945L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, ChatDisplayService::createInvDisplayText);
    }

    @DocStringProvider(id = 1752000288451L, value = """
        Create a `item display` and return the `clickable` text to open that display.
        """)
    private static void registerDisplayItemPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("item", 1752000288451L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, ChatDisplayService::createItemDisplayText);
    }
}
