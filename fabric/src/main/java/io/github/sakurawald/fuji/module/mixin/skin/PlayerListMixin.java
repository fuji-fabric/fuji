package io.github.sakurawald.fuji.module.mixin.skin;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinRestorer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerListMixin {

    @Inject(method = "remove", at = @At("TAIL"))
    private void writeSkinPreferenceOnRemovePlayer(@NotNull ServerPlayerEntity player, CallbackInfo ci) {
        SkinRestorer.getSkinStorage().writeSkinPreference(player.getUuid());
    }

    @Inject(method = "disconnectAllPlayers", at = @At("HEAD"))
    private void writeSkinPreferenceOnDisconnectAllPlayers(CallbackInfo ci) {
        // NOTE: The `remove()` method will not be called for fake-player when stopping the server.
        ServerHelper
            .getOnlinePlayers()
            .forEach(player -> SkinRestorer.getSkinStorage().writeSkinPreference(player.getUuid()));
    }
}
