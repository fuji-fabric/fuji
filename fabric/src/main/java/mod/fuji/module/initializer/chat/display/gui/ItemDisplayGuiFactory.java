package mod.fuji.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.service.display.gui.BaseDisplayGuiFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ItemDisplayGuiFactory extends BaseDisplayGuiFactory {

    private final ItemStack itemStack;

    public ItemDisplayGuiFactory(@NotNull ServerPlayer sharingPlayer, @NotNull ItemStack itemStack) {
        super(sharingPlayer);
        this.itemStack = itemStack;
    }

    @Override
    public @NotNull SlotGuiInterface build(@NotNull ServerPlayer viewingPlayer) {
        /* Make the GUI. */
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_3x3, viewingPlayer, false);
        gui.setTitle(this.title);

        /* Place elements in the GUI. */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.Button.makeSlotPlaceholderButton());
        }

        /* Place the displaying item in the GUI. */
        gui.setSlot(4, itemStack);
        return gui;
    }

}
