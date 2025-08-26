package io.github.sakurawald.fuji.module.initializer.command_toolbox.repair;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class RepairInitializer extends ModuleInitializer {

    @Document(id = 1751825460772L, value = "Repair the item in hand.")
    @CommandNode("repair")
    @CommandRequirement(level = 4)
    private static int $repair(@CommandSource @CommandTarget ServerPlayerEntity player) {
        if (player.getMainHandStack().getDamage() == 0) {
            TextHelper.sendTextByKey(player, "repair.no_damage");
            return CommandHelper.Return.FAILURE;
        }

        player.getMainHandStack().setDamage(0);
        TextHelper.sendTextByKey(player, "repair");
        return CommandHelper.Return.SUCCESS;
    }

}
