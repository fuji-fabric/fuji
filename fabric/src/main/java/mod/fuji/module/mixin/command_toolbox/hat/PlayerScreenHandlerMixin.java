package mod.fuji.module.mixin.command_toolbox.hat;

import org.spongepowered.asm.mixin.Mixin;

#if MC_VER <= MC_1_20_1
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.injection.At;
#elif MC_VER > MC_1_20_1
import net.minecraft.world.inventory.InventoryMenu;
#endif

#if MC_VER <= MC_1_20_1
@Mixin(targets = "net/minecraft/screen/PlayerScreenHandler$1")
public class PlayerScreenHandlerMixin {

    @ModifyExpressionValue(method = "canInsert", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getPreferredEquipmentSlot(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/EquipmentSlot;"), require = 0)
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
@Mixin(InventoryMenu.class)
public class PlayerScreenHandlerMixin {}
#endif

