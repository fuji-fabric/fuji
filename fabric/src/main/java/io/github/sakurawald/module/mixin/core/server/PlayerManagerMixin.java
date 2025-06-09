package io.github.sakurawald.module.mixin.core.server;

import io.github.sakurawald.core.auxiliary.LogUtil;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Mutable
    @Final
    @Shadow
    private List<ServerPlayerEntity> players;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void patchCopyOnWriteArrayListForServerPlayerList(CallbackInfo ci) {
        players = new CopyOnWriteArrayList<>() {
            {
                LogUtil.debug("patch CopyOnWriteArrayList for `players` field in PlayerManager");
            }
        };
    }
}
