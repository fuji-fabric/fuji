package mod.fuji.core.service.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import java.util.ArrayList;
import java.util.List;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.InventoryHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class InventoryDisplayGuiFactory extends BaseDisplayGuiFactory {

    private final SlotGuiInterface parentGui;
    protected final List<ItemStack> armor = new ArrayList<>();
    protected final List<ItemStack> offhand = new ArrayList<>();
    protected final List<ItemStack> main = new ArrayList<>();

    public InventoryDisplayGuiFactory(@Nullable SlotGuiInterface parentGui, @NotNull Component title, @NotNull List<ItemStack> main, @NotNull List<ItemStack> armor, @NotNull List<ItemStack> offhand) {
        super(title);
        this.parentGui = parentGui;
        this.main.addAll(main);
        this.armor.addAll(armor);
        this.offhand.addAll(offhand);
    }

    public InventoryDisplayGuiFactory(@NotNull ServerPlayer sharingPlayer) {
        super(sharingPlayer);
        this.parentGui = null;
        InventoryHelper.getMainStacks(sharingPlayer).forEach(itemStack -> main.add(itemStack.copy()));
        InventoryHelper.getArmorStacks(sharingPlayer).forEach(itemStack -> armor.add(itemStack.copy()));
        InventoryHelper.getOffhandStack(sharingPlayer).forEach(itemStack -> offhand.add(itemStack.copy()));
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Override
    public @NotNull SlotGuiInterface build(@NotNull ServerPlayer viewingPlayer) {
        /* Place the layout elements of the GUI. */
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, viewingPlayer, false) {
            @Override
            public void onClose() {
                if (parentGui != null) {
                    parentGui.open();
                }
            }
        };
        gui.setTitle(this.title);

        for (int i = 0; i < GuiHelper.GENERIC_CONTAINER_LINE_SIZE * 2; i++) {
            gui.setSlot(i, GuiHelper.Button.makeSlotPlaceholderButton());
        }

        /* Place armor items. */
        for (int i = 1; i < 5; i++) {
            gui.setSlot(i, armor.get((5 - 1) - i));
        }

        /* Place offhand item. */
        placeDisplayingItemStack(gui, 7, offhand.get(0), viewingPlayer);

        /* Place main items. */
        for (int i = GuiHelper.GENERIC_CONTAINER_LINE_SIZE * 5; i < GuiHelper.GENERIC_CONTAINER_LINE_SIZE * 6; i++) {
            ItemStack itemStack = main.get(i - GuiHelper.GENERIC_CONTAINER_LINE_SIZE * 5);
            placeDisplayingItemStack(gui, i, itemStack, viewingPlayer);
        }
        for (int i = GuiHelper.GENERIC_CONTAINER_LINE_SIZE * 2; i < GuiHelper.GENERIC_CONTAINER_LINE_SIZE * 5; i++) {
            ItemStack itemStack = main.get(i - GuiHelper.GENERIC_CONTAINER_LINE_SIZE);
            placeDisplayingItemStack(gui, i, itemStack, viewingPlayer);
        }

        return gui;
    }

}
