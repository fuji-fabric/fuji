package mod.fuji.module.mixin.functional.enchantment;

import mod.fuji.core.config.Configs;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.functional.enchantment.EnchantmentInitializer;
import mod.fuji.module.initializer.functional.enchantment.gui.MyEnchantmentScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

    @TestCase(action = "See if a pickaxe gets the max power level in `/enchantment`", targets = "See if the lambda of enchantment context is modified.")
    @ModifyArg(method = "method_17411(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;calculateRequiredExperienceLevel(Lnet/minecraft/util/math/random/Random;IILnet/minecraft/item/ItemStack;)I"), index = 2)
    int modifyTheNumberOfPowerOfProviders(int i) {
        var enchantment = Configs.MAIN_CONTROL_CONFIG.model().modules.functional.enchantment;
        if (enchantment.enable) {
            EnchantmentScreenHandler instance = (EnchantmentScreenHandler) (Object) this;
            if (instance instanceof MyEnchantmentScreenHandler) {
                return EnchantmentInitializer.config.model().enchantment.override_power.power_provider_amount;
            }
        }
        return i;
    }

}
