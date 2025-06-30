package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class InventoryHelper {

    private static final List<EquipmentSlot> PLAYER_ARMOR_SLOTS = List.of(
            EquipmentSlot.HEAD
            , EquipmentSlot.CHEST
            , EquipmentSlot.LEGS
            , EquipmentSlot.FEET);

    public static DefaultedList<ItemStack> getMainStacks(PlayerEntity player) {
        // Main Stacks (1*9 slots + 3*9 slots)

        #if MC_VER <= MC_1_21_4
        return player.getInventory().main;
        #elif MC_VER >= MC_1_21_5
        return player.getInventory().getMainStacks();
        #endif
    }

    public static DefaultedList<ItemStack> getOffhandStack(PlayerEntity player) {
        // Offhand (1 slot) = EquipmentSlot.OFFHAND

        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(1, ItemStack.EMPTY);
        EquipmentSlot offhand = EquipmentSlot.OFFHAND;
        itemStacks.set(0, player.getEquippedStack(offhand));
        return itemStacks;
    }

    public static DefaultedList<ItemStack> getArmorStacks(PlayerEntity player) {
        // Armor (4 slots) = EquipmentSlot.HEAD + EquipmentSlot.CHEST + EquipmentSlot.LEGS + EquipmentSlot.FEET

        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(PLAYER_ARMOR_SLOTS.size(), ItemStack.EMPTY);

        for (int i = 0; i < PLAYER_ARMOR_SLOTS.size(); i++) {
            EquipmentSlot equipmentSlot = PLAYER_ARMOR_SLOTS.get(i);
            itemStacks.set(i, player.getEquippedStack(equipmentSlot));
        }

        return itemStacks;
    }

    public static void setArmorStacks(PlayerEntity player, List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            #if MC_VER < MC_1_21_5
            // NOTE: The armor slot index is reversed. (4 slots)
            player.getInventory().armor.set(3 - i, stacks.get(i));
            #elif MC_VER >= MC_1_21_5
            player.equipment.put(PLAYER_ARMOR_SLOTS.get(i), stacks.get(i));
            #endif
        }
    }

    public static void setOffhandStacks(PlayerEntity player, List<ItemStack> stacks) {
        // It looks like the size of stacks is 0 or 1.
        if (stacks.isEmpty()) return;

        #if MC_VER < MC_1_21_5
        player.getInventory().offHand.set(0, stacks.get(0));
        #elif MC_VER >= MC_1_21_5
        player.equipment.put(EquipmentSlot.OFFHAND, stacks.getFirst());
        #endif

    }

    public static List<DefaultedList<ItemStack>> getCombinedInventory(PlayerEntity player) {
        return ImmutableList.of(
            InventoryHelper.getMainStacks(player)
            , InventoryHelper.getArmorStacks(player)
            , InventoryHelper.getOffhandStack(player));
    }

    public static DefaultedList<ItemStack> getHeldStacks(SimpleInventory inventory) {
        #if MC_VER <= MC_1_20_2
        return inventory.stacks;
        #elif MC_VER > MC_1_20_2
        return inventory.getHeldStacks();
        #endif
    }
}
