package mod.fuji.module.initializer.functional.workbench;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.network.chat.Component;

public class WorkbenchInitializer extends ModuleInitializer {

    @CommandNode("workbench")
    @CommandRequirement(level = 4)
    private static int $workbench(@CommandSource @CommandTarget ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((i, inventory, p) -> new CraftingMenu(i, inventory, ContainerLevelAccess.create(PlayerHelper.getServerWorld(p), p.blockPosition())) {
            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, Component.translatable("container.crafting")));
        player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        return CommandHelper.Return.SUCCESS;
    }
}
