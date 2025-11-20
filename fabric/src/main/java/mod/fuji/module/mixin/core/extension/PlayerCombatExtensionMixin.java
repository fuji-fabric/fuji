package mod.fuji.module.mixin.core.extension;

import mod.fuji.core.extension.PlayerCombatExtension;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class PlayerCombatExtensionMixin implements PlayerCombatExtension {

    @Unique
    public boolean fuji$inCombat;

    @Inject(method = "onEnterCombat", at = @At("RETURN"))
    public void $enterCombat(CallbackInfo ci) {
        fuji$inCombat = true;
    }

    @Inject(method = "onLeaveCombat", at = @At("RETURN"))
    public void $leaveCombat(CallbackInfo ci) {
        fuji$inCombat = false;
    }

    @Override
    public boolean fuji$inCombat() {
        return fuji$inCombat;
    }

}
