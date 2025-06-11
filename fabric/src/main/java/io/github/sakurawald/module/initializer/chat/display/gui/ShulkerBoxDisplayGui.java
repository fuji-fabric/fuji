package io.github.sakurawald.module.initializer.chat.display.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
#if MC_VER <= MC_1_20_4
#elif MC_VER > MC_1_20_4
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
#endif
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;


public class ShulkerBoxDisplayGui extends BaseDisplayGui {

    private final Text title;
    private final ItemStack itemStack;
    private final SimpleGui parentGui;

    public ShulkerBoxDisplayGui(Text title, ItemStack itemStack, SimpleGui parentGui) {
        this.title = title;
        this.itemStack = itemStack;
        this.parentGui = parentGui;
    }

    private static @NotNull Stream<ItemStack> extractItemListFromShulkerBox(ItemStack stack) {

        #if MC_VER <= MC_1_20_4
        NbtCompound blockEntityData = BlockItem.getBlockEntityNbt(stack);
        if (blockEntityData != null) {
            NbtList items = (NbtList) blockEntityData.get("Items");
            if (items == null) return Stream.empty();

            DefaultedList<ItemStack> temp = DefaultedList.ofSize(items.size(), ItemStack.EMPTY);

            items.forEach(item -> {
                NbtCompound itemNbtCompound = (NbtCompound) item;
                int slotIndex = itemNbtCompound.getInt("Slot");
                ItemStack itemStack = ItemStack.fromNbt(itemNbtCompound);

                temp.set(slotIndex, itemStack);
            });
            return temp.stream();
        }
        return Stream.empty();

        #elif MC_VER > MC_1_20_4
        ContainerComponent containerComponent = stack.get(DataComponentTypes.CONTAINER);
        if (containerComponent == null) {
            return Stream.empty();
        }
        return containerComponent.stream();
        #endif

    }

    @Override
    public @NotNull SimpleGui build(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setLockPlayerInventory(true);
        gui.setTitle(this.title);

        /* construct base  */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.makeSlotPlaceholder().getItemStack());
        }
        gui.setSlot(4, itemStack);
        if (this.parentGui != null) {
            gui.setSlot(LINE_SIZE - 1, GuiHelper.makeBackButton(player).setCallback(parentGui::open));
        }

        /* construct items */
        Stream<ItemStack> containerStream = extractItemListFromShulkerBox(itemStack);

        var counter = new Object() {
            int offset = 0;
        };
        containerStream.forEach(item -> {
            gui.setSlot(LINE_SIZE + counter.offset, item.copy());
            counter.offset++;
        });

        return gui;
    }
}
