package io.github.sakurawald.core.auxiliary.minecraft;

import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

@UtilityClass
public class InventoryHelper {

    public static final List<EquipmentSlot> PLAYER_ARMOR_SLOTS = List.of(
            EquipmentSlot.HEAD
            , EquipmentSlot.CHEST
            , EquipmentSlot.LEGS
            , EquipmentSlot.FEET);

    // Main Stacks (1*9 slots + 3*9 slots)
    public static DefaultedList<ItemStack> getMainStacks(PlayerEntity player) {
        return player.getInventory().getMainStacks();
    }

    // Offhand (1 slot) = EquipmentSlot.OFFHAND
    public static DefaultedList<ItemStack> getOffhandStack(PlayerEntity player) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(1, ItemStack.EMPTY);
        itemStacks.set(0, player.getEquippedStack(EquipmentSlot.OFFHAND));
        return itemStacks;
    }

    // Armor (4 slots) = EquipmentSlot.HEAD + EquipmentSlot.CHEST + EquipmentSlot.LEGS + EquipmentSlot.FEET
    public static DefaultedList<ItemStack> getArmorStacks(PlayerEntity player) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(PLAYER_ARMOR_SLOTS.size(), ItemStack.EMPTY);

        for (int i = 0; i < PLAYER_ARMOR_SLOTS.size(); i++) {
            itemStacks.set(i, player.getEquippedStack(PLAYER_ARMOR_SLOTS.get(i)));
        }

        return itemStacks;
    }

    public static void setArmorStacks(PlayerEntity player, List<ItemStack> stacks) {

        for (int i = 0; i < stacks.size(); i++) {
            player.equipment.put(PLAYER_ARMOR_SLOTS.get(i), stacks.get(i));
        }
    }

    public static void setOffhandStacks(PlayerEntity player, List<ItemStack> stacks) {
        // It looks like the size of stacks is 0 or 1.
        if (stacks.isEmpty()) return;

        player.equipment.put(EquipmentSlot.OFFHAND, stacks.getFirst());
    }

    public static List<DefaultedList<ItemStack>> getCombinedInventory(PlayerEntity player) {
        return ImmutableList.of(InventoryHelper.getMainStacks(player)
                , InventoryHelper.getArmorStacks(player)
                , InventoryHelper.getOffhandStack(player));
    }
}
