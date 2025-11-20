package mod.fuji.module.initializer.command_toolbox.trashcan;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class TrashCanInitializer extends ModuleInitializer {

    @Document(id = 1751825455610L, value = "Open a trans can gui.")
    @CommandNode("trashcan")
    private static int $trashcan(@CommandSource @CommandTarget ServerPlayer player) {
        int rows = 3;
        SimpleContainer simpleInventory = new SimpleContainer(rows * 9);

        Component titleText = TextHelper.getTextByKey(player, "trashcan.gui.title");
        player.openMenu(new SimpleMenuProvider((i, inventory, p) -> new ChestMenu(MenuType.GENERIC_9x3, i, inventory, simpleInventory, rows), titleText));
        return CommandHelper.Return.SUCCESS;
    }
}
