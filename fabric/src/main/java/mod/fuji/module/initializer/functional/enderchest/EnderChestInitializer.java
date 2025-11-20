package mod.fuji.module.initializer.functional.enderchest;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.network.chat.Component;


public class EnderChestInitializer extends ModuleInitializer {

    @CommandNode("enderchest")
    @CommandRequirement(level = 4)
    private static int $enderchest(@CommandSource @CommandTarget ServerPlayer player) {
        PlayerEnderChestContainer enderChestInventory = player.getEnderChestInventory();
        player.openMenu(new SimpleMenuProvider((i, inventory, p) -> ChestMenu.threeRows(i, inventory, enderChestInventory), Component.translatable("container.enderchest")));
        player.awardStat(Stats.OPEN_ENDERCHEST);
        return CommandHelper.Return.SUCCESS;
    }
}
