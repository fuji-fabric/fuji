package io.github.sakurawald.fuji.module.mixin.temp_ban;

import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Accessor
    abstract BannedPlayerList getBannedProfiles();

    @Accessor
    abstract BannedIpList getBannedIps();

    // NOTE: The code is used to fix a bug that mojang didn't notice.
    @Inject(method = "checkCanJoin", at = @At(value = "HEAD"))
    void removeInvalidTempBanEntries(CallbackInfoReturnable<Text> cir)
    {
        getBannedProfiles().removeInvalidEntries();
        getBannedIps().removeInvalidEntries();
    }

}
