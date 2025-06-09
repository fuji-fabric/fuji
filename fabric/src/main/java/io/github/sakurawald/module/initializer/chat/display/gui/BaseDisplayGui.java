package io.github.sakurawald.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDisplayGui {

    protected static final int LINE_SIZE = 9;

    protected static void $setSlot(@NotNull SimpleGui gui, int i, @NotNull ItemStack itemStack, SlotClickForDeeperDisplayCallback slotClickForDeeperDisplayCallback) {
        GuiElementBuilder guiElementBuilder = GuiElementBuilder.from(itemStack).setCallback(slotClickForDeeperDisplayCallback);
        if (isShulkerBox(itemStack)) {
            guiElementBuilder.addLoreLine(TextHelper.getTextByKey(gui.getPlayer(), "display.click.prompt"));
        }
        gui.setSlot(i, guiElementBuilder.build());
    }

    public static boolean isShulkerBox(@NotNull ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
    }

    public abstract SimpleGui build(ServerPlayerEntity player);

    protected record SlotClickForDeeperDisplayCallback(SimpleGui parentGui,
                                                       ServerPlayerEntity player) implements GuiElementInterface.ClickCallback {
        @Override
        public void click(int i, ClickType clickType, net.minecraft.screen.slot.SlotActionType clickType1, @NotNull SlotGuiInterface slotGuiInterface) {
            GuiElementInterface slot = slotGuiInterface.getSlot(i);
            if (slot == null) {
                LogUtil.error("a slot in display gui is null.");
                return;
            }

            ItemStack itemStack = slot.getItemStack();
            if (isShulkerBox(itemStack)) {
                ShulkerBoxDisplayGui shulkerBoxDisplayGui = new ShulkerBoxDisplayGui(TextHelper.getTextByKey(player, "display.gui.title", player.getGameProfile().getName()), itemStack, parentGui);
                shulkerBoxDisplayGui.build(player).open();
            }
        }
    }
}
