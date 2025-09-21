package mod.fuji.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.service.display.gui.BaseDisplayGuiFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class ItemDisplayGuiFactory extends BaseDisplayGuiFactory {

    private final ItemStack itemStack;

    public ItemDisplayGuiFactory(@NotNull ServerPlayerEntity sourcePlayer, @NotNull ItemStack itemStack) {
        super(sourcePlayer);
        this.itemStack = itemStack;
    }

    @Override
    public @NotNull SimpleGui build(@NotNull ServerPlayerEntity viewingPlayer) {
        /* Make the GUI. */
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, viewingPlayer, false);
        gui.setTitle(this.title);

        /* Place UI items. */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.Button.makeSlotPlaceholderButton().getItemStack());
        }

        /* Place the displaying item. */
        gui.setSlot(4, itemStack);
        return gui;
    }
}
