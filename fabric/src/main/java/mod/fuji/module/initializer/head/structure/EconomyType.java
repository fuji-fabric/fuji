package mod.fuji.module.initializer.head.structure;

import mod.fuji.core.auxiliary.minecraft.InventoryHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.config.annotation.NotNullEnumType;
import mod.fuji.module.initializer.head.HeadInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@NotNullEnumType
public enum EconomyType {
    ITEM,
    FREE;

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private static boolean tryExtractItems(@NotNull ServerPlayerEntity player, @NotNull ItemStack economyItemStack, int amount) {
        Iterator<DefaultedList<ItemStack>> iterator = InventoryHelper.getCombinedInventory(player).iterator();
        while (iterator.hasNext()) {
            DefaultedList<ItemStack> list = iterator.next();

            for (ItemStack itemStack : list) {
                if (ItemStackHelper.canCombine(itemStack, economyItemStack)
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

    private static @NotNull ItemStack getCostItem() {
        return ItemStackHelper.Parser.parseItemStack(HeadInitializer.config.model().cost_item_type);
    }
}
