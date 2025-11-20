package mod.fuji.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
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

    private final NonNullList<ItemStack> enderChestItems = NonNullList.create();

    public EnderChestDisplayGuiFactory(@NotNull ServerPlayer sourcePlayer) {
        super(sourcePlayer);
        NonNullList<ItemStack> heldStacks = InventoryHelper.getHeldStacks(sourcePlayer.getEnderChestInventory());
        heldStacks.forEach(itemStack -> this.enderChestItems.add(itemStack.copy()));
    }

    @Override
    public @NotNull SimpleGui build(@NotNull ServerPlayer viewingPlayer) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, viewingPlayer, false);
        gui.setTitle(this.title);

        /* Place UI items. */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.Button.makeSlotPlaceholderButton().getItemStack());
        }
        gui.setSlot(4, Items.ENDER_CHEST.getDefaultInstance());

        /* Place container items. */
        SlotClickForDeeperDisplayCallback callback = new SlotClickForDeeperDisplayCallback(gui, viewingPlayer);
        for (int i = 0; i < this.enderChestItems.size(); i++) {
            placeDisplayItemStack(gui, LINE_SIZE + i, this.enderChestItems.get(i), callback);
        }
        return gui;
    }
}
