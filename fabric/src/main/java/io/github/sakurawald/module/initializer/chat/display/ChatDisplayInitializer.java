package io.github.sakurawald.module.initializer.chat.display;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.display.config.model.ChatDisplayConfigModel;
import io.github.sakurawald.module.initializer.chat.display.helper.DisplayHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Document("""
    This module allows players to show things to others:
    1. Show their item in main hand.
    2. Show their inventory.
    3. Show their ender chest.
    """)
public class ChatDisplayInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ChatDisplayConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatDisplayConfigModel.class);
    private static final String DISPLAY_TEXT_PLACEHOLDER = "display";

    private static void broadcastDisplayText(ServerPlayerEntity player, String broadcastTextKey, MutableText displayText) {
        Text broadcastText = TextHelper.getTextByKey(player, broadcastTextKey);
        broadcastText = TextHelper.replaceTextWithMarker(broadcastText, DISPLAY_TEXT_PLACEHOLDER, () -> displayText);
        TextHelper.sendBroadcastByValue(broadcastText);
    }

    @Document("Show your item in main hand.")
    @CommandNode("chat display item")
    private static int $displayItem(@CommandSource ServerPlayerEntity player) {
        broadcastDisplayText(player, "display.item.broadcast", DisplayHelper.createItemDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Show your inventory.")
    @CommandNode("chat display inv")
    private static int $displayInv(@CommandSource ServerPlayerEntity player) {
        broadcastDisplayText(player, "display.inventory.broadcast", DisplayHelper.createInvDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Show your enderchest.")
    @CommandNode("chat display ender")
    private static int $displayEnder(@CommandSource ServerPlayerEntity player) {
        broadcastDisplayText(player, "display.ender_chest.broadcast", DisplayHelper.createEnderDisplayText(player));
        return CommandHelper.Return.SUCCESS;
    }

    private static void registerDisplayEnderPlaceholder() {
        PlaceholderHelper.registerPlayerPlaceholder("ender", DisplayHelper::createEnderDisplayText);
    }

    private static void registerDisplayInvPlaceholder() {
        PlaceholderHelper.registerPlayerPlaceholder("inv", DisplayHelper::createInvDisplayText);
    }

    private static void registerDisplayItemPlaceholder() {
        PlaceholderHelper.registerPlayerPlaceholder("item", DisplayHelper::createItemDisplayText);
    }

    @Override
    protected void registerPlaceholder() {
        registerDisplayItemPlaceholder();
        registerDisplayInvPlaceholder();
        registerDisplayEnderPlaceholder();
    }

}
