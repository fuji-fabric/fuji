package mod.fuji.module.initializer.view.gui;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;

public class EnderChestRedirectScreenFactory extends RedirectScreenHandlerFactory {

    public EnderChestRedirectScreenFactory(ServerPlayer sourcePlayer, String targetPlayerName) {
        super(targetPlayerName, TextHelper.getTextByKey(sourcePlayer, "view.ender.title", targetPlayerName));
    }

    @Override
    protected Container makeTargetInventoryRedirectScreen() {
        return getTargetPlayer().getEnderChestInventory();
    }

    @Override
    protected MenuType<ChestMenu> getTargetInventorySize() {
        return MenuType.GENERIC_9x3;
    }

    @Override
    protected boolean canClick(AbstractContainerMenu screenHandler, int i) {
        return true;
    }
}
