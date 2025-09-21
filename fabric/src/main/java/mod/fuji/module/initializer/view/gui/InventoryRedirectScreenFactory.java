package mod.fuji.module.initializer.view.gui;

import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class InventoryRedirectScreenFactory extends RedirectScreenHandlerFactory {

    public InventoryRedirectScreenFactory(ServerPlayerEntity sourcePlayer, String targetPlayerName) {
        super(targetPlayerName, TextHelper.getTextByKey(sourcePlayer, "view.inv.title", targetPlayerName));
    }

    @Override
    public Inventory makeTargetInventoryRedirectScreen() {
        // NOTE: In newer MC version, the size of PlayerInventory is 43, instead of 41. (There are `saddle` and `body` slot for even player entity.)
        PlayerInventory firstInventory = getTargetPlayer().getInventory();
        SimpleInventory secondInventory = new SimpleInventory(4);

        DoubleInventory doubleInventory = new DoubleInventory(firstInventory, secondInventory);
        doubleInventory.setStack(41, GuiHelper.Validator.makeBannedSlotPlaceholderItemStack());
        doubleInventory.setStack(42, GuiHelper.Validator.makeBannedSlotPlaceholderItemStack());
        doubleInventory.setStack(43, GuiHelper.Validator.makeBannedSlotPlaceholderItemStack());
        doubleInventory.setStack(44, GuiHelper.Validator.makeBannedSlotPlaceholderItemStack());
        return doubleInventory;
    }

    @Override
    public ScreenHandlerType<GenericContainerScreenHandler> getTargetInventorySize() {
        return ScreenHandlerType.GENERIC_9X5;
    }

    @Override
    public boolean canClick(ScreenHandler screenHandler, int i) {
        return !GuiHelper.Validator.isBannedSlotIndex(screenHandler, i);
    }
}
