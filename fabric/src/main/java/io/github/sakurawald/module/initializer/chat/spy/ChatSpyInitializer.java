package io.github.sakurawald.module.initializer.chat.spy;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.chat.spy.config.model.ChatSpyConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;

@CommandNode("chat spy")
@CommandRequirement(level = 4)
public class ChatSpyInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ChatSpyConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatSpyConfigModel.class).autoSaveEveryMinute();

    @CommandNode("toggle")
    @Document("Toggle the chat spy mode.")
    private static int toggle(@CommandSource ServerPlayerEntity player) {
        ChatSpyConfigModel.PerPlayerOptions options = getOptions(player);
        options.enabled = !options.enabled;

        TextHelper.sendMessageByFlag(player, options.enabled);
        return CommandHelper.Return.SUCCESS;
    }

    private static void ensureOptionsExists(ServerPlayerEntity player) {
        String key = player.getGameProfile().getName();
        config.model().getOptions().putIfAbsent(key, new ChatSpyConfigModel.PerPlayerOptions());
    }

    public static ChatSpyConfigModel.PerPlayerOptions getOptions(ServerPlayerEntity player) {
        ensureOptionsExists(player);
        return config.model().getOptions().get(player.getGameProfile().getName());
    }
}
