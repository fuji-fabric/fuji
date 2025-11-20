package mod.fuji.module.mixin.command_toolbox.hat;

import org.spongepowered.asm.mixin.Mixin;

#if MC_VER <= MC_1_20_1
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(targets = "net/minecraft/world/inventory/InventoryMenu$1")
public class PlayerScreenHandlerMixin {

    @ModifyExpressionValue(method = "mayPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getEquipmentSlotForItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;"), require = 0)
    // NOTE: require = 0, due to the mixin fails in forge platform.
    EquipmentSlot allowPlaceAnyItemInHeadEquipmentSlotInSurvivalGameMode(EquipmentSlot original) {
        /* If an item is not Equipment item, it will prefer EquipmentSlot.MAINHAND by default. */
        if (original == EquipmentSlot.MAINHAND) {
            return EquipmentSlot.HEAD;
        }

        /* If the item is an Equipment item, we respect its original slot preference. */
        return original;
    }
}
#elif MC_VER > MC_1_20_1
import net.minecraft.world.inventory.InventoryMenu;
@Mixin(InventoryMenu.class)
public class PlayerScreenHandlerMixin {}
#endif

