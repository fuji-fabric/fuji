package mod.fuji.module.initializer.head.structure;

import mod.fuji.core.auxiliary.minecraft.InventoryHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.config.annotation.NotNullEnumType;
import mod.fuji.module.initializer.head.HeadInitializer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@NotNullEnumType
public enum EconomyType {
    ITEM,
    FREE;

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private static boolean tryExtractItems(@NotNull ServerPlayer player, @NotNull ItemStack economyItemStack, int amount) {
        Iterator<NonNullList<ItemStack>> iterator = InventoryHelper.getCombinedInventory(player).iterator();
        while (iterator.hasNext()) {
            NonNullList<ItemStack> list = iterator.next();

            for (ItemStack itemStack : list) {
                if (ItemStackHelper.canCombine(itemStack, economyItemStack)
                    && !itemStack.isEnchanted()
                    && itemStack.getCount() >= amount
                ) {
                    itemStack.shrink(amount);
                    return true;
                }
            }

        }
        return false;
    }

    public static void tryPurchaseHeads(@NotNull ServerPlayer player, int headsAmount, @NotNull Runnable onPurchase) {
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

    public static Component getCostText() {
        return switch (HeadInitializer.config.model().economy_type) {
            case ITEM -> Component.empty()
                .append(getCostItem().getHoverName())
                .append(Component.nullToEmpty(" × " + HeadInitializer.config.model().cost_item_amount));
            case FREE -> Component.empty();
        };
    }

    private static @NotNull ItemStack getCostItem() {
        return ItemStackHelper.Parser.parseItemStack(HeadInitializer.config.model().cost_item_type);
    }
}
