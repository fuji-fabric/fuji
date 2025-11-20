package mod.fuji.core.service.display.gui;

import com.google.errorprone.annotations.Keep;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
#if MC_VER <= MC_1_20_4
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
#elif MC_VER > MC_1_20_4
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
#endif
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;


public class ShulkerBoxDisplayGuiFactory extends BaseDisplayGuiFactory {

    @Keep
    private static final int SHULKER_BOX_MAX_CAPACITY = 3 * 9;
    private final @NotNull ItemStack shulkerBoxStack;
    private final @Nullable SimpleGui parentGui;

    public ShulkerBoxDisplayGuiFactory(Component title, @NotNull ItemStack shulkerBoxStack, @Nullable SimpleGui parentGui) {
        super(title);
        this.shulkerBoxStack = shulkerBoxStack;
        this.parentGui = parentGui;
    }

    public ShulkerBoxDisplayGuiFactory(ServerPlayer sourcePlayer, @NotNull ItemStack shulkerBoxStack, @Nullable SimpleGui parentGui) {
        super(sourcePlayer);
        this.shulkerBoxStack = shulkerBoxStack;
        this.parentGui = parentGui;
    }

    private static @NotNull Stream<ItemStack> extractItemsFromShulkerBox(ItemStack stack) {

        #if MC_VER <= MC_1_20_4
        NbtCompound blockEntityData = BlockItem.getBlockEntityNbt(stack);
        if (blockEntityData != null) {
            NbtList items = (NbtList) blockEntityData.get("Items");
            if (items == null) return Stream.empty();

            DefaultedList<ItemStack> temp = DefaultedList.ofSize(SHULKER_BOX_MAX_CAPACITY, ItemStack.EMPTY);
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
        ItemContainerContents containerComponent = stack.get(DataComponents.CONTAINER);
        if (containerComponent == null) {
            return Stream.empty();
        }
        return containerComponent.stream();
        #endif

    }

    @Override
    public @NotNull SimpleGui build(ServerPlayer viewingPlayer) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, viewingPlayer, false);
        gui.setTitle(this.title);

        /* Place UI items.  */
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, GuiHelper.Button.makeSlotPlaceholderButton().getItemStack());
        }
        gui.setSlot(4, shulkerBoxStack);
        if (this.parentGui != null) {
            gui.setSlot(LINE_SIZE - 1, GuiHelper.Button.makeBackButton(viewingPlayer).setCallback(parentGui::open));
        }

        /* Place container items. */
        Stream<ItemStack> containerStream = extractItemsFromShulkerBox(shulkerBoxStack);
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
