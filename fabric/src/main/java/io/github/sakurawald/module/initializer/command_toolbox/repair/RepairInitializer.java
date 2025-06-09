package io.github.sakurawald.module.initializer.command_toolbox.repair;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class RepairInitializer extends ModuleInitializer {

    @CommandNode("repair")
    @CommandRequirement(level = 4)
    @Document("Repair the item in hand.")
    private static int $repair(@CommandSource @CommandTarget ServerPlayerEntity player) {
        player.getMainHandStack().setDamage(0);
        TextHelper.sendMessageByKey(player, "repair");
        return CommandHelper.Return.SUCCESS;
    }

}
