package mod.fuji.core.service.display.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.TestCase;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDisplayGuiFactory {

    protected final Component title;
    protected static final int LINE_SIZE = 9;

    protected BaseDisplayGuiFactory(@NotNull Component title) {
        this.title = title;
    }

    protected BaseDisplayGuiFactory(@NotNull ServerPlayer sourcePlayer) {
        this(TextHelper.getTextByKey(sourcePlayer, "display.gui.title", PlayerHelper.getPlayerName(sourcePlayer)));
    }

    protected static void placeDisplayItemStack(@NotNull SimpleGui gui, int slotIndex, @NotNull ItemStack itemStack, @NotNull SlotClickForDeeperDisplayCallback slotClickForDeeperDisplayCallback) {
        /* Support to go into a shulker box. */
        // Add click callback to go into a shulker box.
        GuiElementBuilder guiElementBuilder = GuiElementBuilder
            .from(itemStack)
            .setCallback(slotClickForDeeperDisplayCallback);

        // Add click prompt for shulker box.
        if (isShulkerBox(itemStack)) {
            guiElementBuilder.addLoreLine(TextHelper.getTextByKey(gui.getPlayer(), "display.click.prompt"));
        }
        gui.setSlot(slotIndex, guiElementBuilder.build());
    }

    public abstract SimpleGui build(ServerPlayer viewingPlayer);

    public static boolean isShulkerBox(@NotNull ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem bi
            && bi.getBlock() instanceof ShulkerBoxBlock;
    }

    @TestCase(action = "Create an inventory display that contains a shulker box.", targets = "See if we can go inside the shulker box.")
    public record SlotClickForDeeperDisplayCallback(@NotNull SimpleGui parentGui, @NotNull ServerPlayer viewingPlayer) implements GuiElementInterface.ClickCallback {
        @Override
        public void click(int i, ClickType clickType, net.minecraft.world.inventory.ClickType clickType1, SlotGuiInterface slotGuiInterface) {
            GuiElementInterface slot = slotGuiInterface.getSlot(i);
            if (slot == null) {
                LogUtil.error("A slot in display GUI is null.");
                return;
            }

            /* In any display gui, if the clicked slot is a shulker box, then we can go into the shulker box. */
            ItemStack itemStack = slot.getItemStack();
            if (isShulkerBox(itemStack)) {
                // NOTE: Here we just copy the parent GUI's title, to ensure the title is correct.
                ShulkerBoxDisplayGuiFactory shulkerBoxDisplayGui = new ShulkerBoxDisplayGuiFactory(parentGui.getTitle(), itemStack, parentGui);
                shulkerBoxDisplayGui
                    .build(viewingPlayer)
                    .open();
            }
        }
    }
}
