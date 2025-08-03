package io.github.sakurawald.fuji.module.initializer.head.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.InventoryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.module.initializer.head.HeadInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public enum EconomyType {
    ITEM,
    FREE;

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private static boolean tryExtractItems(ServerPlayerEntity player, @NotNull Item item, int amount) {
        Iterator<DefaultedList<ItemStack>> iterator = InventoryHelper.getCombinedInventory(player).iterator();
        while (iterator.hasNext()) {
            DefaultedList<ItemStack> list = iterator.next();

            for (ItemStack itemStack : list) {
                if (itemStack.getItem().equals(item)
                    && !itemStack.hasEnchantments()
                    && itemStack.getCount() >= amount
                ) {
                    itemStack.decrement(amount);
                    return true;
                }
            }

        }
        return false;
    }

    public static void tryPurchaseHeads(@NotNull ServerPlayerEntity player, int headsAmount, @NotNull Runnable onPurchase) {
        int costItemAmount = headsAmount * HeadInitializer.config.model().cost_item_amount;
        switch (HeadInitializer.config.model().economy_type) {
            case FREE -> onPurchase.run();
            case ITEM -> {
                if (tryExtractItems(player, getCostItem(), costItemAmount)) {
                    onPurchase.run();
                }
            }
        }
    }

    public static Text getCostText() {
        return switch (HeadInitializer.config.model().economy_type) {
            case ITEM -> Text.empty()
                .append(getCostItem().getName())
                .append(Text.of(" × " + HeadInitializer.config.model().cost_item_amount));
            case FREE -> Text.empty();
        };
    }

    private static @NotNull Item getCostItem() {
        return ItemStackHelper.getItem(HeadInitializer.config.model().cost_item_type);
    }
}
