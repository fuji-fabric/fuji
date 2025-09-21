package mod.fuji.module.mixin.whitelist;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.config.mapper.wrapper.GameProfileWrapper;
import net.minecraft.server.Whitelist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Whitelist.class)
public class UserWhiteListMixin {

    /**
     * Once an offline-player join the server, then the offline-uuid will be added to usercache.json.
     * After that, the server will always use the player's offline-uuid in usercache.json to check whitelist (and other list, like ban list, op list).
     * <p>
     * If you use white-list=true with online-mode=false, then the cases is:
     * 1. for online-player, everything is OK.
     * 2. for offline-player, the whitelist will always check the online-uuid, so you need to type /whitelist off to disable whitelist,
     * and let the offline-player join the game, so that the usercache.json can be updated to the offline-uuid.
     * <p>
     * This @Inject makes the whitelist.json only look-up for the player's name, not the uuid.
     *
     * @see Whitelist#isAllowed(GameProfile)
     * @see Whitelist#toString(GameProfile)
     * @see net.minecraft.util.UserCache#add(GameProfile)
     **/
    #if MC_VER < MC_1_21_9
    @ModifyReturnValue(method = "toString*", at = @At("RETURN"))
    String ignoreUUIDAndOnlyComparePlayerName(String original, @Local(argsOnly = true) GameProfile vanillaType)
    #elif MC_VER >= MC_1_21_9
    @ModifyReturnValue(method = "toString*", at = @At("RETURN"))
    String ignoreUUIDAndOnlyComparePlayerName(String original, @Local(argsOnly = true) net.minecraft.server.PlayerConfigEntry vanillaType)
    #endif
    {
        return GameProfileWrapper
            .fromVanillaType(vanillaType)
            .toGameProfile()
            .map(AuthlibHelper::getName)
            .orElse(original);
    }
}
