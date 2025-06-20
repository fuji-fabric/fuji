package io.github.sakurawald.core.service.display.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDisplayGuiFactory {

    protected final Text title;
    protected static final int LINE_SIZE = 9;

    protected BaseDisplayGuiFactory(ServerPlayerEntity sourcePlayer) {
        this.title = TextHelper.getTextByKey(sourcePlayer, "display.gui.title", PlayerHelper.getName(sourcePlayer));
    }

    protected static void placeDisplayItemStack(@NotNull SimpleGui gui, int i, @NotNull ItemStack itemStack, SlotClickForDeeperDisplayCallback slotClickForDeeperDisplayCallback) {
        /* Support to go into a shulker box. */
        // Add click callback to go into a shulker box.
        GuiElementBuilder guiElementBuilder = GuiElementBuilder.from(itemStack).setCallback(slotClickForDeeperDisplayCallback);

        // Add click prompt for shulker box.
        if (isShulkerBox(itemStack)) {
            guiElementBuilder.addLoreLine(TextHelper.getTextByKey(gui.getPlayer(), "display.click.prompt"));
        }
        gui.setSlot(i, guiElementBuilder.build());
    }

    public abstract SimpleGui build(ServerPlayerEntity viewerPlayer);

    public static boolean isShulkerBox(@NotNull ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem bi
            && bi.getBlock() instanceof ShulkerBoxBlock;
    }

    public record SlotClickForDeeperDisplayCallback(SimpleGui parentGui,
                                                       ServerPlayerEntity viewerPlayer) implements GuiElementInterface.ClickCallback {
        @Override
        public void click(int i, ClickType clickType, net.minecraft.screen.slot.SlotActionType clickType1, @NotNull SlotGuiInterface slotGuiInterface) {
            GuiElementInterface slot = slotGuiInterface.getSlot(i);
            if (slot == null) {
                LogUtil.error("A slot in display gui is null.");
                return;
            }

            /* In any display gui, if the clicked slot is a shulker box, then we can go into the shulker box. */
            ItemStack itemStack = slot.getItemStack();
            if (isShulkerBox(itemStack)) {
                ShulkerBoxDisplayGuiFactory shulkerBoxDisplayGui = new ShulkerBoxDisplayGuiFactory(viewerPlayer, itemStack, parentGui);
                shulkerBoxDisplayGui.build(viewerPlayer).open();
            }
        }
    }
}
