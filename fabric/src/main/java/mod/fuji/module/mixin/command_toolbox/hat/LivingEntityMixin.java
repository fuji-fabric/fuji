package mod.fuji.module.mixin.command_toolbox.hat;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER >= MC_1_21_5
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
#endif

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    #if MC_VER >= MC_1_21_5
    @Inject(method = "isEquippableInSlot", at = @At("HEAD"), cancellable = true, require = 0)
    void allowPlaceAnyItemInHeadEquipmentSlotInSurvivalGameMode(ItemStack itemStack, EquipmentSlot equipmentSlot, CallbackInfoReturnable<Boolean> cir){
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (equipmentSlot == EquipmentSlot.HEAD && livingEntity.isAlwaysTicking()){
            cir.setReturnValue(true);
        }
    }
    #endif
}
