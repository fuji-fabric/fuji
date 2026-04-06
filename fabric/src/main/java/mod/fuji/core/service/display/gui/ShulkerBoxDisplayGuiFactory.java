package mod.fuji.core.service.display.gui;

import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.gui.structure.SimpleGuiDuck;
import mod.fuji.core.gui.structure.SlotGuiInterfaceDuck;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;


public class ShulkerBoxDisplayGuiFactory extends BaseDisplayGuiFactory {

    private final @NotNull ItemStack shulkerBoxStack;
    private final @Nullable SlotGuiInterfaceDuck parentGui;

    public ShulkerBoxDisplayGuiFactory(@NotNull Component title, @NotNull ItemStack shulkerBoxStack, @Nullable SlotGuiInterfaceDuck parentGui) {
        super(title);
        this.shulkerBoxStack = shulkerBoxStack;
        this.parentGui = parentGui;
    }

    public ShulkerBoxDisplayGuiFactory(@NotNull ServerPlayer sharingPlayer, @NotNull ItemStack shulkerBoxStack, @Nullable SlotGuiInterfaceDuck parentGui) {
        super(sharingPlayer);
        this.shulkerBoxStack = shulkerBoxStack;
        this.parentGui = parentGui;
    }

    private static @NotNull Stream<ItemStack> extractItemsFromShulkerBox(@NotNull ItemStack stack) {

        #if MC_VER <= MC_1_20_4
        net.minecraft.nbt.CompoundTag blockEntityData = net.minecraft.world.item.BlockItem.getBlockEntityData(stack);
        if (blockEntityData != null) {
            net.minecraft.nbt.ListTag items = (net.minecraft.nbt.ListTag) blockEntityData.get("Items");
            if (items == null) return Stream.empty();

            final int SHULKER_BOX_MAX_CAPACITY = 3 * 9;
            net.minecraft.core.NonNullList<ItemStack> temp = net.minecraft.core.NonNullList.withSize(SHULKER_BOX_MAX_CAPACITY, ItemStack.EMPTY);
            items.forEach(item -> {
                net.minecraft.nbt.CompoundTag itemNbtCompound = (net.minecraft.nbt.CompoundTag) item;
                int slotIndex = itemNbtCompound.getInt("Slot");
                ItemStack itemStack = ItemStack.of(itemNbtCompound);

                temp.set(slotIndex, itemStack);
            });
            return temp.stream();
        }
        return Stream.empty();

        #elif MC_VER > MC_1_20_4
        net.minecraft.world.item.component.ItemContainerContents containerComponent = stack.get(net.minecraft.core.component.DataComponents.CONTAINER);
        if (containerComponent == null) {
            return Stream.empty();
        }
        #endif

        #if MC_VER < MC_26_1
        return containerComponent.stream();
        #elif MC_VER >= MC_26_1
        return containerComponent.allItemsCopyStream();
        #endif
    }

    @Override
    public @NotNull SimpleGuiDuck build(@NotNull ServerPlayer viewingPlayer) {
        SimpleGuiDuck gui = new SimpleGuiDuck(MenuType.GENERIC_9x4, viewingPlayer, false);
        gui.setTitle(this.title);

        /* Place UI items.  */
        for (int i = 0; i < 9; i++) {
            GuiHelper.setSlot(gui, i, GuiHelper.Button.makeSlotPlaceholderButton());
        }
        gui.setSlot(4, shulkerBoxStack);
        if (this.parentGui != null) {
            gui.setSlot(GuiHelper.GENERIC_CONTAINER_LINE_SIZE - 1, GuiHelper.Button.makeBackButton(viewingPlayer).setCallback(parentGui::open));
        }

        /* Place container items. */
        Stream<ItemStack> containerStream = extractItemsFromShulkerBox(shulkerBoxStack);
        var counter = new Object() {
            int offset = 0;
        };
        containerStream.forEach(item -> {
            ItemStack copy = item.copy();
            gui.setSlot(GuiHelper.GENERIC_CONTAINER_LINE_SIZE + counter.offset, copy);
            counter.offset++;
        });

        return gui;
    }
}
