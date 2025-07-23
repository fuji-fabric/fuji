package io.github.sakurawald.fuji.module.mixin.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.module.initializer.skin.provider.MojangSkinProvider;
import io.github.sakurawald.fuji.module.initializer.skin.service.SkinService;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinRestorer;
import java.util.Optional;
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
    private CompletableFuture<Optional<Property>> pendingSkins;

    #if MC_VER <= MC_1_20_1
    @Inject(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"), cancellable = true)
    #elif MC_VER > MC_1_20_1
    @Inject(method = "tickVerify", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"), cancellable = true)
    #endif
    public void waitForSkin(@NotNull CallbackInfo ci) {
        if (pendingSkins == null) {
            pendingSkins = CompletableFuture.supplyAsync(() -> {
                // the first time the player join, his skin is DEFAULT_SKIN (see #applyRestoredSkinHook)
                // then we try to get skin from mojang-server. if this failed, then set his skin to DEFAULT_SKIN
                // note: a fake-player will not trigger waitForSkin()
                LogUtil.info("Fetch skin for {}", profile.getName());

                if (SkinService.isDefaultSkin(profile)) {
                    SkinRestorer.getSkinStorage().setSkinCache(profile.getId(), MojangSkinProvider.fetchSkin(profile.getName()));
                }

                return SkinRestorer.getSkinStorage().getSkinCache(profile.getId());
            });
        }

        // cancel the player's login until we finish fetching his skin
        if (!pendingSkins.isDone()) {
            ci.cancel();
        }
    }

    #if MC_VER <= MC_1_20_1
    @Inject(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
    public void applyTheFetchedSkin(CallbackInfo ci) {
        /* apply the skin if fetched skin is not empty */
        if (pendingSkins != null) {
            Optional<Property> x = Optional.of(SkinService.getDefaultSkin());
            SkinRestorer.applySkin(profile, pendingSkins.getNow(x).get());
        }
    }
    #elif MC_VER > MC_1_20_1
    @Inject(method = "sendSuccessPacket", at = @At("HEAD"))
    public void applyTheFetchedSkin(@NotNull GameProfile gameProfile, CallbackInfo ci) {
        /* apply the skin if fetched skin is not empty */
        if (pendingSkins != null) {
            SkinRestorer.applySkin(gameProfile, pendingSkins.getNow(SkinRestorer.getSkinStorage().getDefaultSkin()));
        }
    }
    #endif

}
