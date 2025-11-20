package mod.fuji.core.service.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.InventoryHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class InventoryDisplayGuiFactory extends BaseDisplayGuiFactory {

    private final SimpleGui parentGui;
    protected final List<ItemStack> armor = new ArrayList<>();
    protected final List<ItemStack> offhand = new ArrayList<>();
    protected final List<ItemStack> main = new ArrayList<>();

    public InventoryDisplayGuiFactory(@Nullable SimpleGui parentGui, Component title, List<ItemStack> main, List<ItemStack> armor, List<ItemStack> offhand) {
        super(title);
        this.parentGui = parentGui;
        this.main.addAll(main);
        this.armor.addAll(armor);
        this.offhand.addAll(offhand);
    }

    public InventoryDisplayGuiFactory(@NotNull ServerPlayer sourcePlayer) {
        super(sourcePlayer);
        this.parentGui = null;
        InventoryHelper.getMainStacks(sourcePlayer).forEach(itemStack -> main.add(itemStack.copy()));
        InventoryHelper.getArmorStacks(sourcePlayer).forEach(itemStack -> armor.add(itemStack.copy()));
        InventoryHelper.getOffhandStack(sourcePlayer).forEach(itemStack -> offhand.add(itemStack.copy()));
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Override
    public @NotNull SimpleGui build(ServerPlayer viewingPlayer) {
        /* Place the placeholder items. */
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, viewingPlayer, false) {
            @Override
            public void onClose() {
                if (parentGui != null) {
                    parentGui.open();
                }
            }
        };
        gui.setTitle(this.title);

        for (int i = 0; i < LINE_SIZE * 2; i++) {
            gui.setSlot(i, GuiHelper.Button.makeSlotPlaceholderButton().getItemStack());
        }

        /* Place armor items. */
        for (int i = 1; i < 5; i++) {
            gui.setSlot(i, armor.get((5 - 1) - i));
        }

        /* Place offhand item. */
        SlotClickForDeeperDisplayCallback slotClickForDeeperDisplayCallback = new SlotClickForDeeperDisplayCallback(gui, viewingPlayer);
        gui.setSlot(7, offhand.get(0), slotClickForDeeperDisplayCallback);

        /* Place main items. */
        for (int i = LINE_SIZE * 5; i < LINE_SIZE * 6; i++) {
            ItemStack itemStack = main.get(i - LINE_SIZE * 5);
            placeDisplayItemStack(gui, i, itemStack, slotClickForDeeperDisplayCallback);
        }
        for (int i = LINE_SIZE * 2; i < LINE_SIZE * 5; i++) {
            ItemStack itemStack = main.get(i - LINE_SIZE);
            placeDisplayItemStack(gui, i, itemStack, slotClickForDeeperDisplayCallback);
        }

        return gui;
    }

}
