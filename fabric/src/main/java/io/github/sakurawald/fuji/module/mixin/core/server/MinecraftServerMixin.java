package io.github.sakurawald.fuji.module.mixin.core.server;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    void injectTheServerInstance(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        LogUtil.debug("Set the default minecraft server to {}", server);
        ServerHelper.setServer(server);
    }
}
