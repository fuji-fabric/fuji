package mod.fuji.module.initializer.command_toolbox.feed;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;


public class FeedInitializer extends ModuleInitializer {

    @Document(id = 1751825164823L, value = "Set food level, saturation level and exhaustion to healthy state.")
    @CommandNode("feed")
    @CommandRequirement(level = 4)
    private static int $feed(@CommandSource @CommandTarget ServerPlayerEntity source) {
        HungerManager foodData = source.getHungerManager();
        foodData.setFoodLevel(20);
        foodData.setSaturationLevel(5);

        TextHelper.sendTextByKey(source, "feed");
        return CommandHelper.Return.SUCCESS;
    }
}
