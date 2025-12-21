package mod.fuji.module.mixin.core.authlib;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameProfile.class, remap = false)
public class GameProfileMixin {

    @Mutable
    @Shadow
    @Final
    private PropertyMap properties;

    /**
     * Mojang makes the PropertyMap immutable collection, and introduces the PropertyMap.EMPTY shared constant.
     * All the players share the same PropertyMap.EMPTY instance, which makes it hard to mutate and manage.
     * Here I allocate a mutable version of PropertyMap instance for each player.
     */
    @Inject(method = "<init>(Ljava/util/UUID;Ljava/lang/String;)V", at = @At("RETURN"))
    void makePropertyMapMutable(CallbackInfo ci) {
        this.properties = AuthlibHelper.makePropertyMap();
    }

}
