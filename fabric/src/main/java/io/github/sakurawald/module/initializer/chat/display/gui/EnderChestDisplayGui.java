package io.github.sakurawald.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;


public class EnderChestDisplayGui extends BaseDisplayGui {

    private final Text title;
    private final DefaultedList<ItemStack> items = DefaultedList.of();

    public EnderChestDisplayGui(Text title, @NotNull ServerPlayerEntity serverPlayer) {
        this.title = title;
        serverPlayer.getEnderChestInventory().getHeldStacks().forEach(itemStack -> this.items.add(itemStack.copy()));
    }

    @Override
    public @NotNull SimpleGui build(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setLockPlayerInventory(true);
        gui.setTitle(this.title);

        /* construct base  */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.makeSlotPlaceholder().getItemStack());
        }
        gui.setSlot(4, Items.ENDER_CHEST.getDefaultStack());

        /* construct items */
        SlotClickForDeeperDisplayCallback slotClickForDeeperDisplayCallback = new SlotClickForDeeperDisplayCallback(gui, player);
        for (int i = 0; i < this.items.size(); i++) {
            $setSlot(gui, LINE_SIZE + i, this.items.get(i), slotClickForDeeperDisplayCallback);
        }
        return gui;
    }
}
