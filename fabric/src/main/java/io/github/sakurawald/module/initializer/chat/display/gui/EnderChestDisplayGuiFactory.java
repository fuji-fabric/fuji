package io.github.sakurawald.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.InventoryHelper;
import io.github.sakurawald.core.service.display.gui.BaseDisplayGuiFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;


public class EnderChestDisplayGuiFactory extends BaseDisplayGuiFactory {

    private final DefaultedList<ItemStack> enderChestItems = DefaultedList.of();

    public EnderChestDisplayGuiFactory(@NotNull ServerPlayerEntity sourcePlayer) {
        super(sourcePlayer);
        DefaultedList<ItemStack> heldStacks = InventoryHelper.getHeldStacks(sourcePlayer.getEnderChestInventory());
        heldStacks.forEach(itemStack -> this.enderChestItems.add(itemStack.copy()));
    }

    @Override
    public @NotNull SimpleGui build(ServerPlayerEntity viewerPlayer) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, viewerPlayer, false);
        gui.setLockPlayerInventory(true);
        gui.setTitle(this.title);

        /* Place UI items. */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.makeSlotPlaceholder().getItemStack());
        }
        gui.setSlot(4, Items.ENDER_CHEST.getDefaultStack());

        /* Place container items. */
        SlotClickForDeeperDisplayCallback slotClickForDeeperDisplayCallback = new SlotClickForDeeperDisplayCallback(gui, viewerPlayer);
        for (int i = 0; i < this.enderChestItems.size(); i++) {
            placeDisplayItemStack(gui, LINE_SIZE + i, this.enderChestItems.get(i), slotClickForDeeperDisplayCallback);
        }
        return gui;
    }
}
