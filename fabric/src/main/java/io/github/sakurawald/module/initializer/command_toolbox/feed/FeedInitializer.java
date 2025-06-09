package io.github.sakurawald.module.initializer.command_toolbox.feed;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;


public class FeedInitializer extends ModuleInitializer {

    @CommandNode("feed")
    @Document("Set food level, saturation level and exhaustion to healthy state.")
    @CommandRequirement(level = 4)
    private static int $feed(@CommandSource @CommandTarget ServerPlayerEntity source) {
        HungerManager foodData = source.getHungerManager();
        foodData.setFoodLevel(20);
        foodData.setSaturationLevel(5);

        TextHelper.sendMessageByKey(source, "feed");
        return CommandHelper.Return.SUCCESS;
    }
}
