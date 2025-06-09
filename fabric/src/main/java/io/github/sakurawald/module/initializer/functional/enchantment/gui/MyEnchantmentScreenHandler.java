package io.github.sakurawald.module.initializer.functional.enchantment.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.jetbrains.annotations.NotNull;

public class MyEnchantmentScreenHandler extends EnchantmentScreenHandler {

    public MyEnchantmentScreenHandler(int i, @NotNull PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext) {
        super(i, playerInventory, screenHandlerContext);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
