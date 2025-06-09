package io.github.sakurawald.module.mixin.disabler.max_player_disabler;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DedicatedPlayerManager.class)
public abstract class DedicatedPlayerManagerMixin {

    @ModifyExpressionValue(
        method = "canBypassPlayerLimit",
        at = @At(
            value = "INVOKE", target = "Lnet/minecraft/server/OperatorList;canBypassPlayerLimit(Lcom/mojang/authlib/GameProfile;)Z"
        )
    )
    public boolean disablePlayerLimit(boolean original, GameProfile profile) {
        return true;
    }

}
