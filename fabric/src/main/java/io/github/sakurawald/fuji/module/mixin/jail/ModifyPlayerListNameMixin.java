package io.github.sakurawald.fuji.module.mixin.jail;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerPlayerEntity.class, priority = 1000 + 1000)
public class ModifyPlayerListNameMixin {

    @ModifyReturnValue(method = "getPlayerListName", at = @At("RETURN"))
    Text modifyPlayerListName(Text original) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        return JailService.modifyDisplayName(original, player);
    }

}
