package mod.fuji.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.InventoryHelper;
import mod.fuji.core.service.display.gui.BaseDisplayGuiFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.NotNull;


public class EnderChestDisplayGuiFactory extends BaseDisplayGuiFactory {

    private final NonNullList<@NotNull ItemStack> enderChestItemStacks = NonNullList.create();

    public EnderChestDisplayGuiFactory(@NotNull ServerPlayer sharingPlayer) {
        super(sharingPlayer);
        NonNullList<@NotNull ItemStack> heldStacks = InventoryHelper.getHeldStacks(sharingPlayer.getEnderChestInventory());
        heldStacks.forEach(itemStack -> this.enderChestItemStacks.add(itemStack.copy()));
    }

    @Override
    public @NotNull SlotGuiInterface build(@NotNull ServerPlayer viewingPlayer) {
        /* Make the GUI. */
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, viewingPlayer, false);
        gui.setTitle(this.title);

        /* Place elements in the GUI. */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.Button.makeSlotPlaceholderButton());
        }
        gui.setSlot(4, Items.ENDER_CHEST.getDefaultInstance());

        /* Place the displaying item stacks. */
        for (int i = 0; i < this.enderChestItemStacks.size(); i++) {
            placeDisplayingItemStack(gui, GuiHelper.GENERIC_CONTAINER_LINE_SIZE + i, this.enderChestItemStacks.get(i), viewingPlayer);
        }
        return gui;
    }
}
