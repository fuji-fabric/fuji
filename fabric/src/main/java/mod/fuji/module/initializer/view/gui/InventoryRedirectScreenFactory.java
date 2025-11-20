package mod.fuji.module.initializer.view.gui;

import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;

public class InventoryRedirectScreenFactory extends RedirectScreenHandlerFactory {

    public InventoryRedirectScreenFactory(ServerPlayer sourcePlayer, String targetPlayerName) {
        super(targetPlayerName, TextHelper.getTextByKey(sourcePlayer, "view.inv.title", targetPlayerName));
    }

    @Override
    public Container makeTargetInventoryRedirectScreen() {
        // NOTE: In newer MC version, the size of PlayerInventory is 43, instead of 41. (There are `saddle` and `body` slot for even player entity.)
        Inventory firstInventory = getTargetPlayer().getInventory();
        SimpleContainer secondInventory = new SimpleContainer(4);

        CompoundContainer doubleInventory = new CompoundContainer(firstInventory, secondInventory);
        doubleInventory.setItem(41, GuiHelper.Validator.makeBannedSlotPlaceholderItemStack());
        doubleInventory.setItem(42, GuiHelper.Validator.makeBannedSlotPlaceholderItemStack());
        doubleInventory.setItem(43, GuiHelper.Validator.makeBannedSlotPlaceholderItemStack());
        doubleInventory.setItem(44, GuiHelper.Validator.makeBannedSlotPlaceholderItemStack());
        return doubleInventory;
    }

    @Override
    public MenuType<ChestMenu> getTargetInventorySize() {
        return MenuType.GENERIC_9x5;
    }

    @Override
    public boolean canClick(AbstractContainerMenu screenHandler, int i) {
        return !GuiHelper.Validator.isBannedSlotIndex(screenHandler, i);
    }
}
