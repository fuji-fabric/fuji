package io.github.sakurawald.fuji.module.mixin.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.AuthlibHelper;
import io.github.sakurawald.fuji.module.initializer.skin.service.SkinService;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow
    private GameProfile profile;

    @Unique
    CompletableFuture<Property> skinFuture;

    #if MC_VER <= MC_1_20_1
    @Inject(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"), cancellable = true)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "tickVerify", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"), cancellable = true)
    #endif
    public void postponeTheLoginUntilTheSkinFetchingIsComplete(@NotNull CallbackInfo ci) {
        // NOTE: A fake-player will not trigger this mixin function. Actually, a fake-player will not even trigger the login process.

        /* Initialize the skin future. */
        if (this.skinFuture == null) {
            this.skinFuture = CompletableFuture.supplyAsync(() -> SkinService.getEffectiveSkin(profile));
        }

        /* Postpone player login until the skin fetching is complete. */
        if (!this.skinFuture.isDone()) {
            ci.cancel();
        }
    }

    #if MC_VER <= MC_1_20_1
    @Inject(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
    void applyTheFetchedSkin(CallbackInfo ci)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "sendSuccessPacket", at = @At("HEAD"))
    void applyTheFetchedSkin(@NotNull GameProfile gameProfile, CallbackInfo ci)
    #endif
    {
        if (this.skinFuture == null) {
            LogUtil.warn("Failed to modify the skin property for player {}. (It seems like the tickVerify() method is modified by other mods.)", profile.getName());
            return;
        }

        /* apply the skin if fetched skin is not empty */
        Property valueIfAbsent = SkinService.getPreferredDefaultSkin();
        AuthlibHelper.modifyGameProfile(profile, skinFuture.getNow(valueIfAbsent));
    }

}
