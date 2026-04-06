package mod.fuji.module.initializer.view.gui;

import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public abstract class RedirectScreenHandlerFactory {

    private final String targetPlayerName;
    private final Component title;

    private boolean onlineEditMode;
    private ServerPlayer targetPlayer;

    public RedirectScreenHandlerFactory(String targetPlayerName, Component title) {
        this.targetPlayerName = targetPlayerName;
        this.title = title;

        // load
        this.loadTargetPlayer();
    }

    protected ServerPlayer getTargetPlayer() {
        return this.targetPlayer;
    }

    private void loadTargetPlayer() {
        ServerPlayer player = PlayerHelper.getPlayerManager().getPlayerByName(targetPlayerName);
        if (player != null) {
            onlineEditMode = true;
            targetPlayer = player;
        } else {
            targetPlayer = PlayerHelper.Loader.loadDummyPlayer(targetPlayerName);
        }
    }

    // the redirect will invalid if online-offline or offline-online.
    private boolean isRedirectValid() {
        if (onlineEditMode) {
            return !targetPlayer.isRemoved();
        }
        return !PlayerHelper.Lookup.isPlayerOnline(targetPlayerName);
    }

    protected abstract Container makeTargetInventoryRedirectScreen();

    protected abstract MenuType<ChestMenu> getTargetInventorySize();

    protected abstract boolean canClick(AbstractContainerMenu screenHandler, int i);

    private void savePlayerData() {
        ServerHelper.getServer().playerDataStorage.save(targetPlayer);
    }

    private ChestMenu makeGenericContainerScreenHandler(int syncId, Inventory sourceInventory, Player source) {
        int rows = GuiHelper.Handler.getGenericContainerRows(getTargetInventorySize());

        return new ChestMenu(getTargetInventorySize(), syncId, sourceInventory, makeTargetInventoryRedirectScreen(), rows) {

            @Override
            #if MC_VER < MC_26_1
            public void clicked(int i, int j, net.minecraft.world.inventory.ClickType slotActionType, Player playerEntity)
            #elif MC_VER >= MC_26_1
            public void clicked(int i, int j, net.minecraft.world.inventory.ContainerInput slotActionType, Player playerEntity)
            #endif
            {
                if (!canClick(this, i)) return;

                // save player data in time, in keep sync if player gets online.
                if (!onlineEditMode) {
                    savePlayerData();
                }
                super.clicked(i, j, slotActionType, playerEntity);
            }

            @Override
            public boolean stillValid(Player playerEntity) {
                return isRedirectValid();
            }

            @Override
            public void removed(Player playerEntity) {
                super.removed(playerEntity);
                PlayerHelper.getPlayerManager().save(getTargetPlayer());
            }
        };
    }

    public SimpleMenuProvider makeFactory() {
        return new SimpleMenuProvider(
            this::makeGenericContainerScreenHandler, this.title);
    }

}
