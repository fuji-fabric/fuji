package io.github.sakurawald.fuji.module.initializer.command_toolbox.feed;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;


public class FeedInitializer extends ModuleInitializer {

    @Document("Set food level, saturation level and exhaustion to healthy state.")
    @CommandNode("feed")
    @CommandRequirement(level = 4)
    private static int $feed(@CommandSource @CommandTarget ServerPlayerEntity source) {
        HungerManager foodData = source.getHungerManager();
        foodData.setFoodLevel(20);
        foodData.setSaturationLevel(5);

        TextHelper.sendMessageByKey(source, "feed");
        return CommandHelper.Return.SUCCESS;
    }
}
