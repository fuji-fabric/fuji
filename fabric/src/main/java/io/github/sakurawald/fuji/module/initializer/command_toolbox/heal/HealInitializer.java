package io.github.sakurawald.fuji.module.initializer.command_toolbox.heal;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class HealInitializer extends ModuleInitializer {

    @Document(id = 1751825152795L, value = "Fill the `health` and `hunger` for the player.")
    @CommandNode("heal")
    @CommandRequirement(level = 4)
    private static int $heal(@CommandSource @CommandTarget ServerPlayerEntity player) {
        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
        TextHelper.sendTextByKey(player, "heal");
        return CommandHelper.Return.SUCCESS;
    }

}
