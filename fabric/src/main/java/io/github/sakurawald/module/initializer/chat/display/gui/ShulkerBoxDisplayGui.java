package io.github.sakurawald.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;


public class ShulkerBoxDisplayGui extends BaseDisplayGui {

    private final Text title;
    private final ItemStack itemStack;
    private final SimpleGui parentGui;

    public ShulkerBoxDisplayGui(Text title, ItemStack itemStack, SimpleGui parentGui) {
        this.title = title;
        this.itemStack = itemStack;
        this.parentGui = parentGui;
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
        gui.setSlot(4, itemStack);
        if (this.parentGui != null) {
            gui.setSlot(LINE_SIZE - 1, GuiHelper.makeBackButton(player).setCallback(parentGui::open));
        }

        /* construct items */
        ContainerComponent containerComponent = itemStack.get(DataComponentTypes.CONTAINER);

        if (containerComponent != null) {
            var counter = new Object() {
                int offset = 0;
            };
            containerComponent.stream().forEach(item -> {
                gui.setSlot(LINE_SIZE + counter.offset, item.copy());
                counter.offset++;
            });
        }

        return gui;
    }
}
