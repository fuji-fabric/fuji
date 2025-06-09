package io.github.sakurawald.module.initializer.chat.display;

import io.github.sakurawald.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.display.config.model.ChatDisplayConfigModel;
import io.github.sakurawald.module.initializer.chat.display.helper.DisplayHelper;

public class ChatDisplayInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ChatDisplayConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatDisplayConfigModel.class);

    private static void registerEnderPlaceholder() {
        PlaceholderHelper.withPlayer("ender", DisplayHelper::createEnderDisplayText);
    }

    private static void registerInvPlaceholder() {
        PlaceholderHelper.withPlayer("inv", DisplayHelper::createInvDisplayText);
    }

    private static void registerItemPlaceholder() {
        PlaceholderHelper.withPlayer("item", DisplayHelper::createItemDisplayText);
    }

    @Override
    protected void registerPlaceholder() {
        registerItemPlaceholder();
        registerInvPlaceholder();
        registerEnderPlaceholder();
    }

}
