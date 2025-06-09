package io.github.sakurawald.module.initializer.command_toolbox.heal;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class HealInitializer extends ModuleInitializer {

    @CommandNode("heal")
    @CommandRequirement(level = 4)
    private static int $heal(@CommandSource @CommandTarget ServerPlayerEntity player) {
        player.setHealth(player.getMaxHealth());
        TextHelper.sendMessageByKey(player, "heal");
        return CommandHelper.Return.SUCCESS;
    }

}
