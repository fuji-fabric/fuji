package io.github.sakurawald.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ItemDisplayGui extends BaseDisplayGui {

    private final Text title;
    private final ItemStack itemStack;

    public ItemDisplayGui(Text title, ItemStack itemStack) {
        this.title = title;
        this.itemStack = itemStack;
    }

    @Override
    public @NotNull SimpleGui build(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false);
        gui.setLockPlayerInventory(true);
        gui.setTitle(this.title);

        /* construct base */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.makeSlotPlaceholder().getItemStack());
        }
        /* construct item */
        gui.setSlot(4, itemStack);
        return gui;
    }
}
