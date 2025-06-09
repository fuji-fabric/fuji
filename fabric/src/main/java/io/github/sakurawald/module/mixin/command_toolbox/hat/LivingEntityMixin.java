package io.github.sakurawald.module.mixin.command_toolbox.hat;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract void equipStack(EquipmentSlot equipmentSlot, ItemStack itemStack);

    @Inject(method = "canEquip", at = @At("HEAD"), cancellable = true)
    void allowPlaceAnyItemInHeadEquipmentSlot(ItemStack itemStack, EquipmentSlot equipmentSlot, CallbackInfoReturnable<Boolean> cir){
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (equipmentSlot == EquipmentSlot.HEAD && livingEntity.isPlayer()){
            cir.setReturnValue(true);
        }
    }
}
