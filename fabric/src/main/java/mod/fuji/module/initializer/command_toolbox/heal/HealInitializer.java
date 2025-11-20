package mod.fuji.module.initializer.command_toolbox.heal;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.level.ServerPlayer;


public class HealInitializer extends ModuleInitializer {

    @Document(id = 1751825152795L, value = "Fill the `health` and `hunger` for the player.")
    @CommandNode("heal")
    @CommandRequirement(level = 4)
    private static int $heal(@CommandSource @CommandTarget ServerPlayer player) {
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        TextHelper.sendTextByKey(player, "heal");
        return CommandHelper.Return.SUCCESS;
    }

}
