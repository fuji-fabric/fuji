package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStartingEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStoppedEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStoppingEvent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(MinecraftServer.class)
public class ServerLifecycleEventMixin {

    @EventProducer(ServerStartingEvent.class)
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"))
    private void produceServerStartingEvent(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerStartingEvent event = new ServerStartingEvent(server);
        EventManager.dispatchEvent(ServerStartingEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(ServerStartedEvent.class)
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;"))
    private void produceServerStartedEvent(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerStartedEvent event = new ServerStartedEvent(server);
        EventManager.dispatchEvent(ServerStartedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(ServerStoppingEvent.class)
    @Inject(method = "shutdown", at = @At("HEAD"))
    private void produceServerStoppingEvent(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerStoppingEvent event = new ServerStoppingEvent(server);
        EventManager.dispatchEvent(ServerStoppingEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

    @EventProducer(ServerStoppedEvent.class)
    @Inject(method = "shutdown", at = @At("TAIL"))
    private void produceServerStoppedEvent(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerStoppedEvent event = new ServerStoppedEvent(server);
        EventManager.dispatchEvent(ServerStoppedEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
