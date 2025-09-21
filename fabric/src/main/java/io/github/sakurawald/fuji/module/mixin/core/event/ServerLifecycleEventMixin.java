package io.github.sakurawald.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartingEvent;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStoppedEvent;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStoppingEvent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(MinecraftServer.class)
public class ServerLifecycleEventMixin {

    @EventProducer(ServerStartingEvent.class)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"), method = "runServer")
    private void produceServerStartingEvent(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerStartingEvent event = new ServerStartingEvent(server);
        EventManager.dispatchEvent(ServerStartingEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(ServerStartedEvent.class)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;", ordinal = 0), method = "runServer")
    private void produceServerStartedEvent(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerStartedEvent event = new ServerStartedEvent(server);
        EventManager.dispatchEvent(ServerStartedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(ServerStoppingEvent.class)
    @Inject(at = @At("HEAD"), method = "shutdown")
    private void produceServerStoppingEvent(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerStoppingEvent event = new ServerStoppingEvent(server);
        EventManager.dispatchEvent(ServerStoppingEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(ServerStoppedEvent.class)
    @Inject(at = @At("TAIL"), method = "shutdown")
    private void produceServerStoppedEvent(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerStoppedEvent event = new ServerStoppedEvent(server);
        EventManager.dispatchEvent(ServerStoppedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
