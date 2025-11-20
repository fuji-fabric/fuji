package mod.fuji.module.mixin.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.module.initializer.skin.service.SkinService;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginNetworkHandlerMixin {

    #if MC_VER <= MC_1_20_1
    @Shadow
    GameProfile gameProfile;
    #elif MC_VER > MC_1_20_1
    @Shadow
    private GameProfile authenticatedProfile;
    #endif

    @Unique
    @Nullable GameProfile getCurrentGameProfile() {
        #if MC_VER <= MC_1_20_1
        return gameProfile;
        #elif MC_VER > MC_1_20_1
        return authenticatedProfile;
        #endif
    }


    @Unique
    CompletableFuture<Property> skinFuture;

    #if MC_VER <= MC_1_20_1
    @Inject(method = "handleAcceptedLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"), cancellable = true)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "verifyLoginAndFinishConnectionSetup", at = @At("HEAD"), cancellable = true)
    #endif
    public void postponeTheLoginUntilTheSkinFetchingIsComplete(@NotNull CallbackInfo ci) {
        // NOTE: A fake-player will not trigger this mixin function. Actually, a fake-player will not even trigger the login process.

        /* Initialize the skin future. */
        if (this.skinFuture == null) {
            this.skinFuture = CompletableFuture.supplyAsync(() -> {
                GameProfile currentGameProfile = getCurrentGameProfile();
                return SkinService.getEffectiveSkin(currentGameProfile);
            });
        }

        /* Postpone player login until the skin fetching is complete. */
        if (!this.skinFuture.isDone()) {
            ci.cancel();
        }
    }

    #if MC_VER <= MC_1_20_1
    @Inject(method = "handleAcceptedLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", ordinal = 0))
    void applyTheFetchedSkin(CallbackInfo ci)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "finishLoginAndWaitForClient", at = @At("HEAD"))
    void applyTheFetchedSkin(@NotNull GameProfile gameProfile, CallbackInfo ci)
    #endif
    {
        GameProfile currentGameProfile = getCurrentGameProfile();
        if (this.skinFuture == null) {
            LogUtil.warn("Failed to modify the skin property for player {}. (It seems like the tickVerify() method is modified by other mods.)", AuthlibHelper.getName(currentGameProfile));
            return;
        }

        /* apply the skin if fetched skin is not empty */
        Property valueIfAbsent = SkinService.getPreferredDefaultSkin();
        AuthlibHelper.modifyGameProfile(currentGameProfile, skinFuture.getNow(valueIfAbsent));
    }

}
