package mod.fuji.module.mixin.temp_ban;

import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {

    @Accessor("bans")
    abstract UserBanList getBannedProfiles();

    @Accessor("ipBans")
    abstract IpBanList getBannedIps();

    // NOTE: The code is used to fix a bug that mojang didn't notice.
    @Inject(method = "canPlayerLogin", at = @At(value = "HEAD"))
    void removeInvalidTempBanEntries(CallbackInfoReturnable<Component> cir)
    {
        getBannedProfiles().removeExpired();
        getBannedIps().removeExpired();
    }

}
