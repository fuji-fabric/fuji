package mod.fuji.core.auxiliary.minecraft;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class InventoryHelper {

    private static final List<EquipmentSlot> PLAYER_ARMOR_SLOTS = List.of(
            EquipmentSlot.HEAD
            , EquipmentSlot.CHEST
            , EquipmentSlot.LEGS
            , EquipmentSlot.FEET);

    /**
 * Main Stacks (1*9 slots + 3*9 slots)
 **/
    public static NonNullList<ItemStack> getMainStacks(@NotNull Player player) {
        #if MC_VER <= MC_1_21_4
        return player.getInventory().items;
        #elif MC_VER >= MC_1_21_5
        return player.getInventory().getNonEquipmentItems();
        #endif
    }

    /**
 * Offhand (1 slot) = EquipmentSlot.OFFHAND
 **/
    public static NonNullList<ItemStack> getOffhandStack(@NotNull Player player) {
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
        EquipmentSlot offhand = EquipmentSlot.OFFHAND;
        itemStacks.set(0, player.getItemBySlot(offhand));
        return itemStacks;
    }

    /**
 * Armor (4 slots) = EquipmentSlot.HEAD + EquipmentSlot.CHEST + EquipmentSlot.LEGS + EquipmentSlot.FEET
 **/
    public static NonNullList<ItemStack> getArmorStacks(@NotNull Player player) {
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(PLAYER_ARMOR_SLOTS.size(), ItemStack.EMPTY);

        for (int i = 0; i < PLAYER_ARMOR_SLOTS.size(); i++) {
            EquipmentSlot equipmentSlot = PLAYER_ARMOR_SLOTS.get(i);
            itemStacks.set(i, player.getItemBySlot(equipmentSlot));
        }

        return itemStacks;
    }

    public static Set<ItemStack> getInventoryStacks(@NotNull Player player) {
        NonNullList<ItemStack> mainStacks = getMainStacks(player);
        NonNullList<ItemStack> offhandStacks = getOffhandStack(player);
        NonNullList<ItemStack> armorStacks = getArmorStacks(player);
        HashSet<ItemStack> stacks = new HashSet<>();
        stacks.addAll(mainStacks);
        stacks.addAll(offhandStacks);
        stacks.addAll(armorStacks);
        return stacks;
    }

    public static void setArmorStacks(@NotNull Player player, @NotNull List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            #if MC_VER < MC_1_21_5
            // NOTE: The armor slot index is reversed. (4 slots)
            player.getInventory().armor.set(3 - i, stacks.get(i));
            #elif MC_VER >= MC_1_21_5
            player.equipment.set(PLAYER_ARMOR_SLOTS.get(i), stacks.get(i));
            #endif
        }
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static void setOffhandStacks(@NotNull Player player, @NotNull List<ItemStack> stacks) {
        // It looks like the size of stacks is 0 or 1.
        if (stacks.isEmpty()) return;

        ItemStack stack = stacks.get(0);

        #if MC_VER <= MC_1_21_4
        player.getInventory().offhand.set(0, stack);
        #elif MC_VER > MC_1_21_4
        player.equipment.set(EquipmentSlot.OFFHAND, stack);
        #endif

    }

    public static List<NonNullList<ItemStack>> getCombinedInventory(@NotNull Player player) {
        return ImmutableList.of(
            InventoryHelper.getMainStacks(player)
            , InventoryHelper.getArmorStacks(player)
            , InventoryHelper.getOffhandStack(player));
    }

    public static NonNullList<ItemStack> getHeldStacks(@NotNull SimpleContainer inventory) {
        #if MC_VER <= MC_1_20_2
        return inventory.items;
        #elif MC_VER > MC_1_20_2
        return inventory.getItems();
        #endif
    }
}
