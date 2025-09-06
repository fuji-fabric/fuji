package io.github.sakurawald.fuji.module.mixin.core.event;

import io.github.sakurawald.fuji.core.event.message.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.event.message.impl.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"), method = "runServer")
    private void beforeSetupServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STARTING.invoker().fire((MinecraftServer) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;", ordinal = 0), method = "runServer")
    private void afterSetupServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STARTED.invoker().fire((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "shutdown")
    private void afterShutdownServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STOPPED.invoker().fire((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    private void beforeShutdownServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STOPPING.invoker().fire((MinecraftServer) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"), method = "tick")
    private void onStartTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerTickEvents.START_SERVER_TICK.invoker().fire((MinecraftServer) (Object) this);
    }

}
