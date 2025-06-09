package io.github.sakurawald.module.initializer.view.gui;

import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class InventoryRedirectScreen extends RedirectScreenHandlerFactory {

    public InventoryRedirectScreen(ServerPlayerEntity sourcePlayer, String targetPlayerName) {
        super(targetPlayerName, TextHelper.getTextByKey(sourcePlayer, "view.inv.title", targetPlayerName));
    }

    @Override
    public Inventory getTargetInventory() {
        PlayerInventory firstInventory = getTargetPlayer().getInventory();
        SimpleInventory secondInventory = new SimpleInventory(Items.BARRIER.getDefaultStack(), Items.BARRIER.getDefaultStack(), Items.BARRIER.getDefaultStack(), Items.BARRIER.getDefaultStack());
        return new DoubleInventory(firstInventory, secondInventory);
    }

    @Override
    public ScreenHandlerType<GenericContainerScreenHandler> getTargetInventorySize() {
        return ScreenHandlerType.GENERIC_9X5;
    }

    @Override
    public boolean canClick(int i) {
        return !GuiHelper.isInvalidSlotInsidePlayerInventory(i);
    }
}
