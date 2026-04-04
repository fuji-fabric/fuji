package mod.fuji.core.service.display.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import java.util.Optional;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.gui.structure.GuiClickCallbackDuck;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDisplayGuiFactory {

    protected final Component title;

    protected BaseDisplayGuiFactory(@NotNull Component title) {
        this.title = title;
    }

    protected BaseDisplayGuiFactory(@NotNull ServerPlayer sharingPlayer) {
        this(TextHelper.getTextByKey(sharingPlayer, "display.gui.title", PlayerHelper.getPlayerName(sharingPlayer)));
    }

    public abstract @NotNull SlotGuiInterface build(@NotNull ServerPlayer viewingPlayer);

    protected static void placeDisplayingItemStack(@NotNull SlotGuiInterface gui, int slotIndex, @NotNull ItemStack itemStack, @NotNull ServerPlayer viewingPlayer) {
        /* Make the recursive item display click callback. */
        RecursiveItemDisplayClickCallback callback = new RecursiveItemDisplayClickCallback(gui, viewingPlayer);

        /* Bind the callback to the element. */
        GuiElementBuilder guiElementBuilder = GuiElementBuilder
            .from(itemStack)
            .setCallback(callback);

        /* Add click prompt for shulker box. */
        if (ItemStackHelper.Kind.isShulkerBox(itemStack)) {
            guiElementBuilder.addLoreLine(TextHelper.getTextByKey(gui.getPlayer(), "display.click.prompt"));
        }

        /* Place the element in the GUI. */
        gui.setSlot(slotIndex, guiElementBuilder);
    }

    @TestCase(action = "Create an inventory display that contains a shulker box.", targets = "See if we can go inside the shulker box.")
    private record RecursiveItemDisplayClickCallback(@NotNull SlotGuiInterface parentGui, @NotNull ServerPlayer viewingPlayer) implements GuiClickCallbackDuck {
        @Override
        public void click(int i, ClickType clickType, net.minecraft.world.inventory.ClickType clickType1, SlotGuiInterface slotGuiInterface) {
            var slot = slotGuiInterface.getSlot(i);
            if (slot == null) {
                LogUtil.error("A slot in display GUI is null.");
                return;
            }

            /* Enter that shulker box if possible. */
            ItemStack itemStack = slot.getItemStack();
            if (ItemStackHelper.Kind.isShulkerBox(itemStack)) {
                // NOTE: Copy the parent GUI's title, for consistency.
                Component childGuiTitle = Optional
                    .ofNullable(parentGui.getTitle())
                    .orElse(GuiHelper.EMPTY_TITLE_TEXT);

                ShulkerBoxDisplayGuiFactory shulkerBoxDisplayGui = new ShulkerBoxDisplayGuiFactory(childGuiTitle, itemStack, parentGui);
                shulkerBoxDisplayGui
                    .build(viewingPlayer)
                    .open();
            }
        }
    }
}
