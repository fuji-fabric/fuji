package io.github.sakurawald.module.initializer.view.gui;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class EnderChestRedirectScreen extends RedirectScreenHandlerFactory {

    public EnderChestRedirectScreen(ServerPlayerEntity sourcePlayer, String targetPlayerName) {
        super(targetPlayerName, TextHelper.getTextByKey(sourcePlayer, "view.ender.title", targetPlayerName));
    }

    @Override
    protected Inventory getTargetInventory() {
        return getTargetPlayer().getEnderChestInventory();
    }

    @Override
    protected ScreenHandlerType<GenericContainerScreenHandler> getTargetInventorySize() {
        return ScreenHandlerType.GENERIC_9X3;
    }

    @Override
    protected boolean canClick(int i) {
        return true;
    }
}
