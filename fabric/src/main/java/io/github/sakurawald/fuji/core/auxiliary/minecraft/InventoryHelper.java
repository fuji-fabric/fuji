package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.google.common.collect.ImmutableList;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class InventoryHelper {

    private static final List<EquipmentSlot> PLAYER_ARMOR_SLOTS = List.of(
            EquipmentSlot.HEAD
            , EquipmentSlot.CHEST
            , EquipmentSlot.LEGS
            , EquipmentSlot.FEET);

    @ForDeveloper("Main Stacks (1*9 slots + 3*9 slots)")
    public static DefaultedList<ItemStack> getMainStacks(@NotNull PlayerEntity player) {
        #if MC_VER <= MC_1_21_4
        return player.getInventory().main;
        #elif MC_VER >= MC_1_21_5
        return player.getInventory().getMainStacks();
        #endif
    }

    @ForDeveloper("Offhand (1 slot) = EquipmentSlot.OFFHAND")
    public static DefaultedList<ItemStack> getOffhandStack(@NotNull PlayerEntity player) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(1, ItemStack.EMPTY);
        EquipmentSlot offhand = EquipmentSlot.OFFHAND;
        itemStacks.set(0, player.getEquippedStack(offhand));
        return itemStacks;
    }

    @ForDeveloper("Armor (4 slots) = EquipmentSlot.HEAD + EquipmentSlot.CHEST + EquipmentSlot.LEGS + EquipmentSlot.FEET")
    public static DefaultedList<ItemStack> getArmorStacks(@NotNull PlayerEntity player) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(PLAYER_ARMOR_SLOTS.size(), ItemStack.EMPTY);

        for (int i = 0; i < PLAYER_ARMOR_SLOTS.size(); i++) {
            EquipmentSlot equipmentSlot = PLAYER_ARMOR_SLOTS.get(i);
            itemStacks.set(i, player.getEquippedStack(equipmentSlot));
        }

        return itemStacks;
    }

    public static Set<ItemStack> getInventoryStacks(@NotNull PlayerEntity player) {
        DefaultedList<ItemStack> mainStacks = getMainStacks(player);
        DefaultedList<ItemStack> offhandStacks = getOffhandStack(player);
        DefaultedList<ItemStack> armorStacks = getArmorStacks(player);
        HashSet<ItemStack> stacks = new HashSet<>();
        stacks.addAll(mainStacks);
        stacks.addAll(offhandStacks);
        stacks.addAll(armorStacks);
        return stacks;
    }

    public static void setArmorStacks(@NotNull PlayerEntity player, @NotNull List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            #if MC_VER < MC_1_21_5
            // NOTE: The armor slot index is reversed. (4 slots)
            player.getInventory().armor.set(3 - i, stacks.get(i));
            #elif MC_VER >= MC_1_21_5
            player.equipment.put(PLAYER_ARMOR_SLOTS.get(i), stacks.get(i));
            #endif
        }
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static void setOffhandStacks(@NotNull PlayerEntity player, @NotNull List<ItemStack> stacks) {
        // It looks like the size of stacks is 0 or 1.
        if (stacks.isEmpty()) return;

        ItemStack stack = stacks.get(0);

        #if MC_VER < MC_1_21_5
        player.getInventory().offHand.set(0, stack);
        #elif MC_VER >= MC_1_21_5
        player.equipment.put(EquipmentSlot.OFFHAND, stack);
        #endif

    }

    public static List<DefaultedList<ItemStack>> getCombinedInventory(@NotNull PlayerEntity player) {
        return ImmutableList.of(
            InventoryHelper.getMainStacks(player)
            , InventoryHelper.getArmorStacks(player)
            , InventoryHelper.getOffhandStack(player));
    }

    public static DefaultedList<ItemStack> getHeldStacks(@NotNull SimpleInventory inventory) {
        #if MC_VER <= MC_1_20_2
        return inventory.stacks;
        #elif MC_VER > MC_1_20_2
        return inventory.getHeldStacks();
        #endif
    }
}
