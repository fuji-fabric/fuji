package io.github.sakurawald.fuji.module.initializer.view.gui;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class EnderChestRedirectScreenFactory extends RedirectScreenHandlerFactory {

    public EnderChestRedirectScreenFactory(ServerPlayerEntity sourcePlayer, String targetPlayerName) {
        super(targetPlayerName, TextHelper.getTextByKey(sourcePlayer, "view.ender.title", targetPlayerName));
    }

    @Override
    protected Inventory makeTargetInventoryRedirectScreen() {
        return getTargetPlayer().getEnderChestInventory();
    }

    @Override
    protected ScreenHandlerType<GenericContainerScreenHandler> getTargetInventorySize() {
        return ScreenHandlerType.GENERIC_9X3;
    }

    @Override
    protected boolean canClick(ScreenHandler screenHandler, int i) {
        return true;
    }
}
