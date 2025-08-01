package io.github.sakurawald.fuji.module.mixin.core.server;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @SuppressWarnings("ShadowModifiers")
    @Mutable
    @Final
    @Shadow
    List<ServerPlayerEntity> players;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void patchCopyOnWriteArrayListForPlayersInServerWorld(CallbackInfo ci) {
        ServerWorld thiz = (ServerWorld) (Object) this;
        players = new CopyOnWriteArrayList<>() {
            {
                LogUtil.debug("Patch CopyOnWriteArrayList for `players` field in ServerWorld {}", RegistryHelper.toIdString(thiz));
            }
        };
    }
}
