package io.github.sakurawald.module.mixin.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.service.gameprofile_fetcher.MojangProfileFetcher;
import io.github.sakurawald.module.initializer.skin.structure.SkinRestorer;
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
    private CompletableFuture<Property> pendingSkins;

    @Inject(method = "tickVerify", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"), cancellable = true)
    public void waitForSkin(@NotNull CallbackInfo ci) {
        if (pendingSkins == null) {
            pendingSkins = CompletableFuture.supplyAsync(() -> {
                // the first time the player join, his skin is DEFAULT_SKIN (see #applyRestoredSkinHook)
                // then we try to get skin from mojang-server. if this failed, then set his skin to DEFAULT_SKIN
                // note: a fake-player will not trigger waitForSkin()
                LogUtil.info("fetch skin for {}", profile.getName());

                if (SkinRestorer.getSkinStorage().isDefaultSkin(profile)) {
                    SkinRestorer.getSkinStorage().setSkin(profile.getId(), MojangProfileFetcher.fetchOnlineSkin(profile.getName()));
                }

                return SkinRestorer.getSkinStorage().getSkin(profile.getId());
            });
        }

        // cancel the player's login until we finish fetching his skin
        if (!pendingSkins.isDone()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendSuccessPacket", at = @At("HEAD"))
    public void applyTheFetchedSkin(@NotNull GameProfile gameProfile, CallbackInfo ci) {
        /* apply the skin if fetched skin is not empty */
        if (pendingSkins != null) {
            SkinRestorer.applySkin(gameProfile, pendingSkins.getNow(SkinRestorer.getSkinStorage().getDefaultSkin()));
        }
    }
}
