package io.github.sakurawald.fuji.module.mixin.jail;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerEntity.class, priority = 1000 + 1000)
public class ModifyDisplayNameMixin {

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    Text modifyDisplayName(Text original) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        return JailService.modifyDisplayName(original, player);
    }

}
