package io.github.sakurawald.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.InventoryUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;


public class InventoryDisplayGui extends BaseDisplayGui {

    private final Text title;
    private final DefaultedList<ItemStack> armor = DefaultedList.of();
    private final DefaultedList<ItemStack> offhand = DefaultedList.of();
    private final DefaultedList<ItemStack> items = DefaultedList.of();

    public InventoryDisplayGui(Text title, @NotNull ServerPlayerEntity player) {
        this.title = title;
        PlayerInventory inventory = player.getInventory();
        InventoryUtil.getArmorStacks(player).forEach(itemStack -> armor.add(itemStack.copy()));
        InventoryUtil.getOffhandStack(player).forEach(itemStack -> offhand.add(itemStack.copy()));
        InventoryUtil.getMainStacks(player).forEach(itemStack -> items.add(itemStack.copy()));
    }

    @Override
    public @NotNull SimpleGui build(ServerPlayerEntity player) {
        /* construct base */
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setLockPlayerInventory(true);
        gui.setTitle(this.title);

        for (int i = 0; i < LINE_SIZE * 2; i++) {
            gui.setSlot(i, GuiHelper.makeSlotPlaceholder().getItemStack());
        }

        /* construct armor */
        for (int i = 1; i < 5; i++) {
            gui.setSlot(i, armor.get((5 - 1) - i));
        }

        /* construct offhand */
        SlotClickForDeeperDisplayCallback slotClickForDeeperDisplayCallback = new SlotClickForDeeperDisplayCallback(gui, player);
        gui.setSlot(7, offhand.getFirst(), slotClickForDeeperDisplayCallback);

        /* construct items */
        for (int i = LINE_SIZE * 5; i < LINE_SIZE * 6; i++) {
            ItemStack itemStack = items.get(i - LINE_SIZE * 5);
            $setSlot(gui, i, itemStack, slotClickForDeeperDisplayCallback);
        }
        for (int i = LINE_SIZE * 2; i < LINE_SIZE * 5; i++) {
            ItemStack itemStack = items.get(i - LINE_SIZE);
            $setSlot(gui, i, itemStack, slotClickForDeeperDisplayCallback);
        }
        return gui;
    }

}
