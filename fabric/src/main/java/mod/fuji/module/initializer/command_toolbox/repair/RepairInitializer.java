package mod.fuji.module.initializer.command_toolbox.repair;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.level.ServerPlayer;


public class RepairInitializer extends ModuleInitializer {

    @Document(id = 1751825460772L, value = "Repair the item in hand.")
    @CommandNode("repair")
    @CommandRequirement(level = 4)
    private static int $repair(@CommandSource @CommandTarget ServerPlayer player) {
        if (player.getMainHandItem().getDamageValue() == 0) {
            TextHelper.sendTextByKey(player, "repair.no_damage");
            return CommandHelper.Return.FAILURE;
        }

        player.getMainHandItem().setDamageValue(0);
        TextHelper.sendTextByKey(player, "repair");
        return CommandHelper.Return.SUCCESS;
    }

}
