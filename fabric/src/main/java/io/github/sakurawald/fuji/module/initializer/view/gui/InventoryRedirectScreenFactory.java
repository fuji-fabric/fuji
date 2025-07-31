package io.github.sakurawald.fuji.module.initializer.view.gui;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Items;
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
        PlayerInventory firstInventory = getTargetPlayer().getInventory();
        SimpleInventory secondInventory = new SimpleInventory(Items.BARRIER.getDefaultStack(), Items.BARRIER.getDefaultStack(), Items.BARRIER.getDefaultStack(), Items.BARRIER.getDefaultStack());
        return new DoubleInventory(firstInventory, secondInventory);
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
