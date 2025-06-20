package io.github.sakurawald.core.service.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;


public class InventoryDisplayGui extends BaseDisplayGui {

    private final DefaultedList<ItemStack> armor = DefaultedList.of();
    private final DefaultedList<ItemStack> offhand = DefaultedList.of();
    private final DefaultedList<ItemStack> main = DefaultedList.of();

    public InventoryDisplayGui(@NotNull ServerPlayerEntity sourcePlayer) {
        super(sourcePlayer);
        InventoryHelper.getArmorStacks(sourcePlayer).forEach(itemStack -> armor.add(itemStack.copy()));
        InventoryHelper.getOffhandStack(sourcePlayer).forEach(itemStack -> offhand.add(itemStack.copy()));
        InventoryHelper.getMainStacks(sourcePlayer).forEach(itemStack -> main.add(itemStack.copy()));
    }

    @Override
    public @NotNull SimpleGui build(ServerPlayerEntity viewerPlayer) {
        /* Place the placeholder items. */
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, viewerPlayer, false);
        gui.setLockPlayerInventory(true);
        gui.setTitle(this.title);

        for (int i = 0; i < LINE_SIZE * 2; i++) {
            gui.setSlot(i, GuiHelper.makeSlotPlaceholder().getItemStack());
        }

        /* Place armor items. */
        for (int i = 1; i < 5; i++) {
            gui.setSlot(i, armor.get((5 - 1) - i));
        }

        /* Place offhand item. */
        SlotClickForDeeperDisplayCallback slotClickForDeeperDisplayCallback = new SlotClickForDeeperDisplayCallback(gui, viewerPlayer);
        gui.setSlot(7, offhand.get(0), slotClickForDeeperDisplayCallback);

        /* Place main items. */
        for (int i = LINE_SIZE * 5; i < LINE_SIZE * 6; i++) {
            ItemStack itemStack = main.get(i - LINE_SIZE * 5);
            placeDisplayItemStack(gui, i, itemStack, slotClickForDeeperDisplayCallback);
        }
        for (int i = LINE_SIZE * 2; i < LINE_SIZE * 5; i++) {
            ItemStack itemStack = main.get(i - LINE_SIZE);
            placeDisplayItemStack(gui, i, itemStack, slotClickForDeeperDisplayCallback);
        }
        return gui;
    }

}
