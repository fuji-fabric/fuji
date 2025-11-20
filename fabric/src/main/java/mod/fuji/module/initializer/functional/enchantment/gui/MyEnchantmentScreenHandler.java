package mod.fuji.module.initializer.functional.enchantment.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.NotNull;

public class MyEnchantmentScreenHandler extends EnchantmentMenu {

    public MyEnchantmentScreenHandler(int i, @NotNull Inventory playerInventory, ContainerLevelAccess screenHandlerContext) {
        super(i, playerInventory, screenHandlerContext);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
