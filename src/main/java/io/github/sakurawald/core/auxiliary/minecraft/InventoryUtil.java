package io.github.sakurawald.core.auxiliary.minecraft;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@UtilityClass
public class InventoryUtil {

    /**
     * @return The hot-bar + 3*9 slots
     */
    public static DefaultedList<ItemStack> getMainStacks(PlayerEntity player) {
        return player.getInventory().getMainStacks();
    }

    // Offhand = EquipmentSlot.OFFHAND
    public static DefaultedList<ItemStack> getOffhandStack(PlayerEntity player) {
        return DefaultedList.copyOf(player.getEquippedStack(EquipmentSlot.OFFHAND));
    }

    // Armor = EquipmentSlot.HEAD + EquipmentSlot.CHEST + EquipmentSlot.LEGS + EquipmentSlot.FEET
    public static DefaultedList<ItemStack> getArmorStack(PlayerEntity player) {
        return DefaultedList.copyOf(
            player.getEquippedStack(EquipmentSlot.HEAD)
            , player.getEquippedStack(EquipmentSlot.CHEST)
            , player.getEquippedStack(EquipmentSlot.LEGS)
            , player.getEquippedStack(EquipmentSlot.FEET)
        );
    }
}
