package io.github.sakurawald.module.mixin.skin;

import io.github.sakurawald.module.initializer.skin.structure.SkinRestorer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)

public abstract class PlayerListMixin {

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayerList();

    @Inject(method = "remove", at = @At("TAIL"))
    private void remove(@NotNull ServerPlayerEntity player, CallbackInfo ci) {
        SkinRestorer.getSkinStorage().writeSkin(player.getUuid());
    }

    @Inject(method = "disconnectAllPlayers", at = @At("HEAD"))
    private void disconnectAllPlayers(CallbackInfo ci) {
        getPlayerList().forEach(player -> SkinRestorer.getSkinStorage().writeSkin(player.getUuid()));
    }
}
