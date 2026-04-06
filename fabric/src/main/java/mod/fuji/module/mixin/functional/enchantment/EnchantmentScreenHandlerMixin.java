package mod.fuji.module.mixin.functional.enchantment;

import mod.fuji.core.config.Configs;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.functional.enchantment.EnchantmentInitializer;
import mod.fuji.module.initializer.functional.enchantment.gui.MyEnchantmentScreenHandler;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnchantmentMenu.class)
public class EnchantmentScreenHandlerMixin {

    @TestCase(action = "See if a pickaxe gets the max power level in `/enchantment`", targets = "See if the lambda of enchantment context is modified.")
    #if MC_VER < MC_26_1
    @ModifyArg(method = "method_17411(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"), index = 2)
    #elif MC_VER >= MC_26_1
    @ModifyArg(method = "lambda$slotsChanged$0(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"), index = 2)
    #endif
    int modifyTheNumberOfPowerOfProviders(int i)
    {
        var enchantment = Configs.MAIN_CONTROL_CONFIG.model().modules.functional.enchantment;
        if (enchantment.enable) {
            EnchantmentMenu instance = (EnchantmentMenu) (Object) this;
            if (instance instanceof MyEnchantmentScreenHandler) {
                return EnchantmentInitializer.config.model().enchantment.override_power.power_provider_amount;
            }
        }
        return i;
    }

}
