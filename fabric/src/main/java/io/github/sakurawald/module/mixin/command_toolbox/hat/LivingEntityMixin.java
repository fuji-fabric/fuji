package io.github.sakurawald.module.mixin.command_toolbox.hat;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER >= MC_1_21_5
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.item.ItemStack;
#endif

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    #if MC_VER >= MC_1_21_5
    @Inject(method = "canEquip", at = @At("HEAD"), cancellable = true)
    void allowPlaceAnyItemInHeadEquipmentSlot(ItemStack itemStack, EquipmentSlot equipmentSlot, CallbackInfoReturnable<Boolean> cir){
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (equipmentSlot == EquipmentSlot.HEAD && livingEntity.isPlayer()){
            cir.setReturnValue(true);
        }
    }
    #endif
}
