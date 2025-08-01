package io.github.sakurawald.fuji.module.initializer.view.gui;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public abstract class RedirectScreenHandlerFactory {

    private final String targetPlayerName;
    private final Text title;

    private boolean onlineEditMode;
    private ServerPlayerEntity targetPlayer;

    public RedirectScreenHandlerFactory(String targetPlayerName, Text title) {
        this.targetPlayerName = targetPlayerName;
        this.title = title;

        // load
        this.loadTargetPlayer();
    }

    protected ServerPlayerEntity getTargetPlayer() {
        return this.targetPlayer;
    }

    private void loadTargetPlayer() {
        ServerPlayerEntity player = ServerHelper.getServer().getPlayerManager().getPlayer(targetPlayerName);
        if (player != null) {
            onlineEditMode = true;
            targetPlayer = player;
        } else {
            targetPlayer = PlayerHelper.loadServerPlayerEntity(targetPlayerName);
        }
    }

    // the redirect will invalid if online-offline or offline-online.
    private boolean isRedirectValid() {
        if (onlineEditMode) {
            return !targetPlayer.isRemoved();
        }
        return !PlayerHelper.isPlayerOnline(targetPlayerName);
    }

    protected abstract Inventory makeTargetInventoryRedirectScreen();

    protected abstract ScreenHandlerType<GenericContainerScreenHandler> getTargetInventorySize();

    protected abstract boolean canClick(ScreenHandler screenHandler, int i);

    private void savePlayerData() {
        ServerHelper.getServer().saveHandler.savePlayerData(targetPlayer);
    }

    private GenericContainerScreenHandler makeGenericContainerScreenHandler(int syncId, PlayerInventory sourceInventory, PlayerEntity source) {
        int rows = GuiHelper.Handler.getGenericContainerRows(getTargetInventorySize());

        return new GenericContainerScreenHandler(getTargetInventorySize(), syncId, sourceInventory, makeTargetInventoryRedirectScreen(), rows) {

            @Override
            public void onSlotClick(int i, int j, SlotActionType slotActionType, PlayerEntity playerEntity) {
                if (!canClick(this, i)) return;

                // save player data in time, in keep sync if player gets online.
                if (!onlineEditMode) {
                    savePlayerData();
                }
                super.onSlotClick(i, j, slotActionType, playerEntity);
            }

            @Override
            public boolean canUse(PlayerEntity playerEntity) {
                return isRedirectValid();
            }

            @Override
            public void onClosed(PlayerEntity playerEntity) {
                super.onClosed(playerEntity);
                PlayerHelper.getPlayerManager().savePlayerData(getTargetPlayer());
            }
        };
    }

    public SimpleNamedScreenHandlerFactory makeFactory() {
        return new SimpleNamedScreenHandlerFactory(
            this::makeGenericContainerScreenHandler, this.title);
    }

}
